package com.bookingbot.session;

import java.time.LocalDate;

public class UserSession {
    private UserState state = UserState.START;
    private String selectedService;
    private LocalDate selectedDate;
    private String selectedTime;
    private String userName;
    private String userPhone;
    private LocalDate calendarOffset = LocalDate.now().withDayOfMonth(1);
    private Long editingSlotId;

    // Геттеры и сеттеры
    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public String getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(String selectedService) {
        this.selectedService = selectedService;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    public String getSelectedTime() {
        return selectedTime;
    }

    public void setSelectedTime(String selectedTime) {
        this.selectedTime = selectedTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public LocalDate getCalendarOffset() {
        return calendarOffset;
    }

    public void shiftCalendarOffset(int months) {
        this.calendarOffset = this.calendarOffset.plusMonths(months);
    }

    public Long getEditingSlotId() {
        return editingSlotId;
    }

    public void setEditingSlotId(Long editingSlotId) {
        this.editingSlotId = editingSlotId;
    }
}
