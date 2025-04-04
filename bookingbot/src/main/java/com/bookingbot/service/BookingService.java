package com.bookingbot.service;

import com.bookingbot.model.Booking;
import com.bookingbot.model.TimeSlot;
import com.bookingbot.repository.BookingRepository;
import com.bookingbot.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service

public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;

    public BookingService(BookingRepository bookingRepository, TimeSlotRepository timeSlotRepository) {
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    // ✅ Создание новой записи
    public Booking createBooking(Long userId, String serviceType, LocalDate date, LocalTime time, String name, String phone) {
        Booking booking = Booking.builder()
                .userId(userId)
                .serviceType(serviceType)
                .bookingDate(date)
                .bookingTime(time)
                .name(name)
                .phone(phone)
                .build();
        TimeSlot slot = timeSlotRepository.findByDateAndTime(date, time)
                .orElseThrow(() -> new RuntimeException("Слот не найден"));
        slot.setAvailable(false);
        timeSlotRepository.save(slot);

        return bookingRepository.save(booking);
    }



    // ✅ Получение всех записей на конкретный день
    public List<Booking> getBookingsByDate(LocalDate date) {
        return bookingRepository.findAll().stream()
                .filter(booking -> date.equals(booking.getBookingDate()))
                .toList();
    }



    // ✅ Удаление записи по ID
    public void deleteBooking(Long bookingId) {
        bookingRepository.deleteById(bookingId);
    }

    // ✅ Дополнительное сохранение
    public void save(Booking booking) {
        bookingRepository.save(booking);
    }

    public Optional<Booking> findByDateAndTime(LocalDate date, LocalTime time) {
        return bookingRepository.findByBookingDateAndBookingTime(date, time);
    }

}
