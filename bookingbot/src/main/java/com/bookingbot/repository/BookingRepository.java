package com.bookingbot.repository;

import com.bookingbot.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.reminderSent = false AND b.bookingDate = :date AND b.bookingTime BETWEEN :start AND :end")
    List<Booking> findBookingsForReminder(@Param("date") LocalDate date,
                                          @Param("start") LocalTime start,
                                          @Param("end") LocalTime end);
    Optional<Booking> findByBookingDateAndBookingTime(LocalDate date, LocalTime time);

    // TODO: тут можно добавить специфичные методы поиска, если это нужно бизнесу
}
