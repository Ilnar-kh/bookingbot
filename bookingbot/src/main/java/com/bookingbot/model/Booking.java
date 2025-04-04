package com.bookingbot.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_appointments")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false)
    private Long userId;

    @Column(name = "service_type", nullable = false)
    private String serviceType;

 @Column(name = "booking_date", nullable = false)
 private LocalDate bookingDate;

    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    @Column(nullable = false)
    private String name;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;


    public Booking(Long userId, String serviceType, LocalDate bookingDate, LocalTime bookingTime, String name, String phone) {
        this.userId = userId;
        this.serviceType = serviceType;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.name = name;
        this.phone = phone;
    }

    public Booking() {
        // обязательно нужен Hibernate
    }


    // --- Getters and Setters ---


    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

   public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    // --- Builder pattern for convenience ---

    public static BookingBuilder builder() {
        return new BookingBuilder();
    }

    public static class BookingBuilder {
        private Long userId;
        private String serviceType;
        private LocalDate bookingDate;
        private LocalTime bookingTime;
        private String name;
        private String phone;

        public BookingBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public BookingBuilder serviceType(String serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public BookingBuilder bookingDate(LocalDate bookingDate) {
            this.bookingDate = bookingDate;
            return this;
        }

        public BookingBuilder bookingTime(LocalTime bookingTime) {
            this.bookingTime = bookingTime;
            return this;
        }


        public BookingBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BookingBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Booking build() {
            return new Booking(userId, serviceType, bookingDate, bookingTime, name, phone);
        }
    }


}
