package com.bookingbot.service;

import com.bookingbot.model.TimeSlot;
import com.bookingbot.repository.TimeSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    @Autowired
    public TimeSlotService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    public List<TimeSlot> getSlotsByDate(LocalDate date) {
        //return timeSlotRepository.findByDateAndIsAvailableTrue(date);
        List<TimeSlot> slots = timeSlotRepository.findAvailableByDate(date);
        System.out.println("Найдено слотов: " + slots.size());
        return slots;
    }

    public List<TimeSlot> getAllSlotsByDate(LocalDate date) {
        return timeSlotRepository.findByDate(date); // без фильтра по isAvailable
    }

    public void updateSlotTime(Long slotId, LocalTime newTime) {
        Optional<TimeSlot> optionalSlot = timeSlotRepository.findById(slotId);
        if (optionalSlot.isPresent()) {
            TimeSlot slot = optionalSlot.get();
            slot.setTime(newTime);
            timeSlotRepository.save(slot);
        }
    }

    @Transactional
    public void deleteSlotById(Long id) {
        timeSlotRepository.deleteById(id);
    }

}
