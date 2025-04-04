package com.bookingbot.session;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserSessionService {

    private final Map<Long, UserSession> sessions = new HashMap<>();

    private UserSession getSession(Long userId) {
        return sessions.computeIfAbsent(userId, id -> new UserSession());
    }

    public void setState(Long userId, UserState state) {
        getSession(userId).setState(state);
    }

    public UserState getState(Long userId) {
        return getSession(userId).getState();
    }

    public void setService(Long userId, String service) {
        getSession(userId).setSelectedService(service);
    }

    public String getService(Long userId) {
        return getSession(userId).getSelectedService();
    }

    public void setDate(Long userId, java.time.LocalDate date) {
        getSession(userId).setSelectedDate(date);
    }

    public java.time.LocalDate getDate(Long userId) {
        return getSession(userId).getSelectedDate();
    }

    public void setTime(Long userId, String time) {
        getSession(userId).setSelectedTime(time);
    }

    public String getTime(Long userId) {
        return getSession(userId).getSelectedTime();
    }

    public void setName(Long userId, String name) {
        getSession(userId).setUserName(name);
    }

    public String getName(Long userId) {
        return getSession(userId).getUserName();
    }

    public void setPhone(Long userId, String phone) {
        getSession(userId).setUserPhone(phone);
    }

    public String getPhone(Long userId) {
        return getSession(userId).getUserPhone();
    }

    public java.time.LocalDate getCalendarOffset(Long userId) {
        return getSession(userId).getCalendarOffset();
    }

    public void shiftCalendar(Long userId, int months) {
        getSession(userId).shiftCalendarOffset(months);
    }

    public void setEditingSlotId(Long userId, Long slotId) {
        getSession(userId).setEditingSlotId(slotId);
    }

    public Long getEditingSlotId(Long userId) {
        return getSession(userId).getEditingSlotId();
    }

    public void clearEditingSlotId(Long userId) {
        getSession(userId).setEditingSlotId(null);
    }

    public void clearSession(Long userId) {
        sessions.remove(userId);
    }
}
