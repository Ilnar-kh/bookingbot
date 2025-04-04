package com.bookingbot.service;

import com.bookingbot.bot.Bot;
import com.bookingbot.model.Booking;
import com.bookingbot.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReminderService {

    private final BookingRepository bookingRepository;
    private final Bot bot;

    public ReminderService(BookingRepository bookingRepository, Bot bot) {
        this.bookingRepository = bookingRepository;
        this.bot = bot;
    }

    @Scheduled(fixedRate = 1 * 60 * 1000) // каждые 15 минут
    public void sendReminders() {
        System.out.println("⏰ Метод sendReminders вызван");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Время, за 2 часа до записи
        LocalTime targetTime = now.plusHours(2);

        // Интервал поиска: ±2 минуты, чтобы не пропустить
        LocalTime targetStart = targetTime.minusMinutes(2);
        LocalTime targetEnd = targetTime.plusMinutes(2);
//        LocalTime targetStart = now.plusHours(2).minusMinutes(5); // интервал 5 минут
//        LocalTime targetEnd = now.plusHours(2).plusMinutes(5);

        List<Booking> upcoming = bookingRepository.findBookingsForReminder(today, targetStart, targetEnd);

        for (Booking booking : upcoming) {
            try {
                String text = String.format("""
        🔔 Напоминание о записи!
        
        📅 Дата: %s
        ⏰ Время: %s
        🧾 Услуга: %s

        Мы ждём вас!
        """, booking.getBookingDate(), booking.getBookingTime(), booking.getServiceType());


                bot.sendMessage(booking.getUserId(), text); // где messageText — это String

                booking.setReminderSent(true); // чтобы больше не отправлялось
                bookingRepository.save(booking);
            } catch (Exception e) {
                e.printStackTrace(); // или логировать
            }
        }
    }
}
