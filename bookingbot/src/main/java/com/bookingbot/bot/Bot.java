package com.bookingbot.bot;

import com.bookingbot.model.Booking;
import com.bookingbot.model.TimeSlot;
import com.bookingbot.service.AdminService;
import com.bookingbot.service.BookingService;
import com.bookingbot.service.TimeSlotService;
import com.bookingbot.session.UserSessionService;
import com.bookingbot.session.UserState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

@Component
public class Bot extends TelegramLongPollingBot {

    private final BookingService bookingService;
    private final UserSessionService sessionService;
    private final TimeSlotService timeSlotService;
    @Autowired
    private AdminService adminService;

    @Value("botToken")
    private String botToken;

    @Value("botUsername")
    private String botUsername;

    private final Set<Long> adminIds = Set.of(
            **********L // ← сюда впиши свой Telegram ID
    );


    @Autowired
    public Bot(BookingService bookingService, UserSessionService sessionService, TimeSlotService timeSlotService) {
        this.bookingService = bookingService;
        this.sessionService = sessionService;
        this.timeSlotService = timeSlotService;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            UserState state = sessionService.getState(userId);

            if (text.equals("/start")) {
                sessionService.clearSession(userId); // сбрасываем сессию пользователя, если есть
                handleStart(userId);    // показываем меню выбора услуги
                return; // прерываем дальнейшую обработку
            }

            if (text.equals("/list")) {
                if (adminIds.contains(userId)) {
                    sessionService.setState(userId, UserState.ADMIN_VIEW_DATE);
                    sessionService.clearSession(userId);// сбросим всё, вдруг были клиентские данные
                    sessionService.setState(userId, UserState.ADMIN_VIEW_DATE);
                    System.out.println("👑 Проверка на админа: " + userId + " is admin? " + adminIds.contains(userId));
                    sendAdminCalendar(userId); // показываем календарь
                } else {
                    sendMessage(userId, "⛔ Эта команда доступна только администраторам.");
                }
                return;
            }



            switch (state) {
                case START -> handleStart(userId);
                case SELECT_SERVICE -> handleServiceSelection(userId, text);
                case SELECT_DATE -> handleDateSelection(userId, text);
                case SELECT_TIME -> handleTimeSelection(userId, text);
                case INPUT_NAME -> handleNameInput(userId, text);
                case INPUT_PHONE -> handlePhoneInput(userId, text);
                case ADMIN_INPUT_NEW_TIME -> {
                    try {
                        LocalTime newTime = LocalTime.parse(text); // формат HH:mm
                        Long slotId = sessionService.getEditingSlotId(userId);
                        timeSlotService.updateSlotTime(slotId, newTime); // реализация ниже
                        sessionService.clearEditingSlotId(userId);
                        sessionService.setState(userId, UserState.IDLE); // вернём в обычное состояние
                        sendMessage(userId, "✅ Время успешно обновлено!");
                    } catch (Exception e) {
                        sendMessage(userId, "❌ Неверный формат времени. Введите в формате HH:mm (например, 14:30).");
                    }
                }

                case ADMIN_VIEW_DATE -> {
                    // ничего не делаем — просто оставляем стейт, если вдруг кто-то ввёл руками текст
                    sendMessage(userId, "Вы в админ-режиме. Выберите дату через календарь 📅.");
                }

                default -> sendMessage(userId, "Извините, я не понял команду.");
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callback = update.getCallbackQuery();
            Long userId = callback.getMessage().getChatId();
            String data = callback.getData();

            if (data.equals("/list")) {
                if (adminIds.contains(userId)) {
                    sessionService.clearSession(userId);
                    sessionService.setState(userId, UserState.ADMIN_VIEW_DATE);
                    sendAdminCalendar(userId);
                } else {
                    sendMessage(userId, "⛔ Эта команда доступна только администраторам.");
                }
                return;
            }

            // ✅ Сначала обрабатываем подтверждение или отмену
            if (data.equals("CONFIRM_BOOKING")) {
                saveBooking(userId);
                return;
            } else if (data.equals("CANCEL_BOOKING")) {
                sessionService.clearSession(userId);
                sendMessage(userId, "Запись отменена. Чтобы начать заново — введите любую команду.");
                return;
            }


            if (sessionService.getState(userId) == UserState.ADMIN_VIEW_DATE && data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate selectedDate = LocalDate.parse(data);
                showAdminSlotsForDate(userId, selectedDate);
                return;
            }

            // Дальше — обычная логика
            if (data.equals("NEXT_MONTH") || data.equals("PREV_MONTH")) {
                sessionService.shiftCalendar(userId, data.equals("NEXT_MONTH") ? 1 : -1);
                sendDateCalendarKeyboard(userId);

            } else if (data.startsWith("EDIT_SLOT_")) {
                    Long slotId = Long.parseLong(data.replace("EDIT_SLOT_", ""));
                    sessionService.setEditingSlotId(userId, slotId);
                    sessionService.setState(userId, UserState.ADMIN_INPUT_NEW_TIME);
                    sendMessage(userId, "Введите новое время для слота в формате HH:mm:");
                    return;
                }

            if (data.startsWith("DELETE_SLOT_")) {
                Long slotId = Long.parseLong(data.replace("DELETE_SLOT_", ""));
                timeSlotService.deleteSlotById(slotId);
                sendMessage(userId, "Слот удалён.");
                sendAdminCalendar(userId);
                return;
            }

            if (sessionService.getState(userId) == UserState.SELECT_SERVICE) {
                handleServiceSelection(userId, data);
            } else if (data.matches("\\d{4}-\\d{2}-\\d{2}")) {
                if (sessionService.getState(userId) == UserState.ADMIN_VIEW_DATE) {
                    LocalDate selectedDate = LocalDate.parse(data);
                    showAdminSlotsForDate(userId, selectedDate); // 👈 вызываем админ-функцию
                } else {
                    handleDateSelection(userId, data); // обычный пользователь
                }
                return;
            } else if (data.startsWith("TIME_")) {
                handleTimeSelection(userId, data.replace("TIME_", ""));
            }
        }
    }


    private void handleStart(Long userId) {
        sessionService.setState(userId, UserState.SELECT_SERVICE);
        SendMessage message = new SendMessage();
        message.setChatId(userId.toString());
        message.setText("Добро пожаловать!\uD83D\uDC4B  \n" +
                "Рады видеть вас здесь \uD83D\uDE0A  \n" +
                "Пожалуйста, выберите услугу, которая вам интересна:");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(
                InlineKeyboardButton.builder().text("Стрижка \uD83D\uDC87").callbackData("Стрижка \uD83D\uDC87").build(),
                InlineKeyboardButton.builder().text("Маникюр \uD83D\uDC85").callbackData("Маникюр \uD83D\uDC85").build()
        ));
        rows.add(List.of(
                InlineKeyboardButton.builder().text("Массаж \uD83D\uDC86").callbackData("Массаж \uD83D\uDC86").build(),
                InlineKeyboardButton.builder().text("Косметология \uD83D\uDC84").callbackData("Косметология \uD83D\uDC84").build()
        ));

        if (adminService.isAdmin(userId)) {
            InlineKeyboardButton adminButton = new InlineKeyboardButton("📋 Управление записями");
            adminButton.setCallbackData("/list");
            rows.add(List.of(adminButton));
        }

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleServiceSelection(Long userId, String service) {
        sessionService.setService(userId, service);
        sessionService.setState(userId, UserState.SELECT_DATE);
        sendMessage(userId, "✅ Отличный выбор: " + service + "\nТеперь давайте подберём удобную дату для записи " +
                "\uD83D\uDCC5");
        sendDateCalendarKeyboard(userId);
    }

    private void sendDateCalendarKeyboard(Long userId) {
        try {
            LocalDate baseDate = sessionService.getCalendarOffset(userId);
            YearMonth yearMonth = YearMonth.from(baseDate);
            LocalDate firstDay = yearMonth.atDay(1);
            int shift = (firstDay.getDayOfWeek().getValue() + 6) % 7;

            String monthTitle = yearMonth.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru"));
            String capitalizedMonth = monthTitle.substring(0, 1).toUpperCase() + monthTitle.substring(1);
            int year = yearMonth.getYear();

            SendMessage message = new SendMessage();
            message.setChatId(userId.toString());
            message.setText("📅 " + capitalizedMonth + " " + year + "\nВыберите подходящую для вас дату:\n");

            List<List<InlineKeyboardButton>> rows = new ArrayList<>();
            rows.add(Arrays.stream(new String[]{"Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс"})
                    .map(d -> InlineKeyboardButton.builder().text(d).callbackData("IGNORE").build())
                    .toList());

            List<InlineKeyboardButton> currentRow = new ArrayList<>();
            for (int i = 0; i < shift; i++) {
                currentRow.add(InlineKeyboardButton.builder().text(" ").callbackData("IGNORE").build());
            }

            int daysInMonth = yearMonth.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate currentDate = yearMonth.atDay(day);
                currentRow.add(InlineKeyboardButton.builder()
                        .text(String.valueOf(day))
                        .callbackData(currentDate.toString())
                        .build());
                if (currentRow.size() == 7) {
                    rows.add(currentRow);
                    currentRow = new ArrayList<>();
                }
            }
            if (!currentRow.isEmpty()) {
                while (currentRow.size() < 7) {
                    currentRow.add(InlineKeyboardButton.builder().text(" ").callbackData("IGNORE").build());
                }
                rows.add(currentRow);
            }

            rows.add(List.of(
                    InlineKeyboardButton.builder().text("« Пред").callbackData("PREV_MONTH").build(),
                    InlineKeyboardButton.builder().text("След »").callbackData("NEXT_MONTH").build()
            ));

            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
            message.setReplyMarkup(markup);
            execute(message);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(userId, "Ошибка при построении календаря.");
        }
    }

    private void sendAdminCalendar(Long adminId) {
        sessionService.clearSession(adminId); // Сначала сброс
        sessionService.setState(adminId, UserState.ADMIN_VIEW_DATE); // Потом установка стейта
        sendMessage(adminId, "Выберите дату для просмотра записей 📅:");
        sendDateCalendarKeyboard(adminId);
    }



    private void handleDateSelection(Long userId, String dateStr) {
        try {

//            // ❗ Админов не пускаем в клиентскую логику на всякий случай
//            if (adminIds.contains(userId)) {
//                System.out.println("🚫 handleDateSelection вызван для админа. Прерываем.");
//                return;
//            }

            LocalDate date = LocalDate.parse(dateStr);
            sessionService.setDate(userId, date);
            sessionService.setState(userId, UserState.SELECT_TIME);
            sendAvailableTimeSlots(userId, date);
        } catch (Exception e) {
            sendMessage(userId, "Ошибка при обработке даты. Попробуйте снова.");
        }
    }

    private void handleTimeSelection(Long userId, String time) {
        sessionService.setTime(userId, time);
        sessionService.setState(userId, UserState.INPUT_NAME);
        sendMessage(userId, "Пожалуйста, введите ваше имя ✍:\n");
    }

    private void handleNameInput(Long userId, String name) {
        sessionService.setName(userId, name);
        sessionService.setState(userId, UserState.INPUT_PHONE);
        sendMessage(userId, "Отлично, а теперь введите ваш номер телефона \uD83D\uDCF1:\n");
    }

    private void handlePhoneInput(Long userId, String phone) {
        sessionService.setPhone(userId, phone);
        sessionService.setState(userId, UserState.CONFIRMATION);

        String summary = """
        📋 Проверьте данные:
        Услуга 🧾: %s
        Дата 📅: %s
        Время ⏰: %s
        Имя 👤: %s
        Телефон 📞: %s
        """.formatted(
                sessionService.getService(userId),
                sessionService.getDate(userId),
                sessionService.getTime(userId),
                sessionService.getName(userId),
                sessionService.getPhone(userId)
        );

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        buttons.add(List.of(
                InlineKeyboardButton.builder().text("✅ Подтвердить").callbackData("CONFIRM_BOOKING").build(),
                InlineKeyboardButton.builder().text("❌ Отмена").callbackData("CANCEL_BOOKING").build()
        ));

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(buttons)
                .build();

        sendMessage(userId, summary, markup);
    }


    public void sendMessage(Long UserId, String text) {
        SendMessage message = new SendMessage(UserId.toString(), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAvailableTimeSlots(Long userId, LocalDate date) {
        List<TimeSlot> slots = timeSlotService.getSlotsByDate(date);

        if (slots.isEmpty()) {
            sendMessage(userId, "На выбранную дату свободных слотов нет 😔");
            return;
        }

        slots.sort(Comparator.comparing(TimeSlot::getTime));

        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (TimeSlot slot : slots) {
            String timeLabel = slot.getTime().toString();
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(timeLabel)
                    .callbackData("TIME_" + timeLabel)
                    .build();
            buttons.add(List.of(button));
        }

        InlineKeyboardMarkup keyboard = InlineKeyboardMarkup.builder()
                .keyboard(buttons)
                .build();

        sendMessage(userId, "Свободное время на выбранную дату ⏰: \n", keyboard);
    }

    private void showAdminSlotsForDate(Long adminId, LocalDate date) {
        List<TimeSlot> slots = timeSlotService.getAllSlotsByDate(date); // получаем все
        if (slots.isEmpty()) {
            sendMessage(adminId, "📭 Нет слотов на эту дату.");
            return;
        }

        // Сортируем по времени
        slots.sort(Comparator.comparing(TimeSlot::getTime));

        for (TimeSlot slot : slots) {
            String timeText = slot.getTime().toString();
            String availability = slot.isAvailable() ? "🟢 Свободен" : "🔴 Занят";

            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("🕒 Время: ").append(timeText).append("\n");
            messageBuilder.append("Статус: ").append(availability).append("\n");

            if (!slot.isAvailable()) {
                // Если слот занят — найдём соответствующую запись
                Optional<Booking> bookingOpt = bookingService.findByDateAndTime(date, slot.getTime());
                bookingOpt.ifPresent(booking -> {
                    messageBuilder.append("👤 Имя: ").append(booking.getName()).append("\n");
                    messageBuilder.append("📞 Телефон: ").append(booking.getPhone()).append("\n");
                    messageBuilder.append("🧾 Услуга: ").append(booking.getServiceType()).append("\n");
                    messageBuilder.append("🗓 Создано: ").append(booking.getCreatedAt().toLocalDate()).append("\n");
                });
            }

            // Кнопки для редактирования и удаления
            List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
            buttons.add(List.of(
                    InlineKeyboardButton.builder().text("🗑 Удалить").callbackData("DELETE_SLOT_" + slot.getId()).build(),
                    InlineKeyboardButton.builder().text("✏ Изменить").callbackData("EDIT_SLOT_" + slot.getId()).build()
            ));

            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder().keyboard(buttons).build();
            sendMessage(adminId, messageBuilder.toString(), markup);
        }
    }




    public void sendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(markup);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    private void saveBooking(Long userId) {
        bookingService.createBooking(
                userId,
                sessionService.getService(userId),
                sessionService.getDate(userId),
                LocalTime.parse(sessionService.getTime(userId)), // Важно: time хранится как строка
                sessionService.getName(userId),
                sessionService.getPhone(userId)
        );

        sendMessage(userId, "\uD83C\uDF89 Спасибо!  \n" +
                "Ваша запись успешно создана ✅  \n" +
                "Мы свяжемся с вами в ближайшее время.  \n" +
                "Хорошего дня! ☀\uFE0F\n");
        // 🔔 Уведомление админам
        String adminNotification = """
            📢 Новая запись!
            👤 Имя: %s
            📞 Телефон: %s
            🧾 Услуга: %s
            📅 Дата: %s
            ⏰ Время: %s
            """.formatted(
                sessionService.getName(userId),
                sessionService.getPhone(userId),
                sessionService.getService(userId),
                sessionService.getDate(userId),
                sessionService.getTime(userId)
        );

        // Отправим всем админам (если их несколько)
        for (Long adminId : adminIds) {
            sendMessage(adminId, adminNotification);
        }

        sessionService.clearSession(userId);
    }

}
