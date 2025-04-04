package com.bookingbot.repository;

import com.bookingbot.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    @Query("SELECT t FROM TimeSlot t WHERE t.date = :date AND t.available = true")
    List<TimeSlot> findAvailableByDate(@Param("date") LocalDate date);
    Optional<TimeSlot> findByDateAndTime(LocalDate date, LocalTime time);
    @Query("SELECT MAX(ts.date) FROM TimeSlot ts")
    Optional<LocalDate> findMaxDate();

    List<TimeSlot> findByDate(LocalDate date);

    // === TODO: Здесь можно добавлять методы выборки под конкретный бизнес ===

    // Пример: найти все слоты, если захотим сортировать по времени
    // List<TimeSlot> findAllByOrderByTimeAsc();
}
