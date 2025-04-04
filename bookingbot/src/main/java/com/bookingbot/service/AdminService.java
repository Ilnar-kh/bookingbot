package com.bookingbot.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AdminService {

    // Здесь хранятся Telegram ID админов
    private static final Set<Long> ADMINS = Set.of(
            1083640393L // Замените на свой Telegram ID
    );

    public boolean isAdmin(Long chatId) {
        return ADMINS.contains(chatId);
    }
}
