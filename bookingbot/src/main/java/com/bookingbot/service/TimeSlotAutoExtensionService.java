package com.bookingbot.service;

import com.bookingbot.model.TimeSlot;
import com.bookingbot.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TimeSlotAutoExtensionService {

    private final TimeSlotRepository timeSlotRepository;

    @Autowired
    public TimeSlotAutoExtensionService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    // Задайте слоты, которые будут добавляться каждый день
    private final List<LocalTime> defaultTimes = List.of(
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0),
            LocalTime.of(14, 0),
            LocalTime.of(15, 0),
            LocalTime.of(16, 0),
            LocalTime.of(17, 0)
    );

    /**
     * Запускается каждый день в 00:01, проверяет количество уникальных дней в таблице.
     * Если меньше 30 — добавляет новые дни с указанными слотами.
     */
    @Scheduled(cron = "0 1 0 * * *") // каждый день в 00:01
//    @Scheduled(fixedRate = 10000) // каждые 10 секунд
//    public void autoGenerateSlots() {
//    ensureSlotsFor30DaysAhead();
//    }

    public void ensureSlotsFor30DaysAhead() {
        LocalDate today = LocalDate.now();
        LocalDate maxDateInDb = timeSlotRepository.findMaxDate().orElse(today.minusDays(1));
        LocalDate targetDate = today.plusDays(29); // 30 дней включая сегодня

        while (maxDateInDb.isBefore(targetDate)) {
            maxDateInDb = maxDateInDb.plusDays(1);
            for (LocalTime time : defaultTimes) {
                TimeSlot slot = new TimeSlot();
                slot.setDate(maxDateInDb);
                slot.setTime(time);
                slot.setAvailable(true);
                timeSlotRepository.save(slot);
            }
            System.out.println("Добавлены слоты на " + maxDateInDb);
        }
    }
}
