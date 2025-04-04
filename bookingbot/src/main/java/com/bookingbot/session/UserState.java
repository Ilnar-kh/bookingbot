package com.bookingbot.session;

// Состояния (этапы) взаимодействия пользователя с ботом
public enum UserState {
    START,              // Начальное состояние
    SELECT_SERVICE,     // Пользователь выбирает услугу
    SELECT_DATE,        // Пользователь выбирает дату
    SELECT_TIME,        // Пользователь выбирает время
    INPUT_NAME,         // Пользователь вводит своё имя
    INPUT_PHONE,        // Пользователь вводит номер телефона
    CONFIRMATION,        // Подтверждение записи
    IDLE,
    WAITING_FOR_NAME,
    WAITING_FOR_PHONE,
    ADMIN_VIEW_DATE,
    ADMIN_INPUT_NEW_TIME
    ;

    }
