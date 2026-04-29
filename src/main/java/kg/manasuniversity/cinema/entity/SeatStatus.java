package kg.manasuniversity.cinema.entity;

public enum SeatStatus {
        AVAILABLE, // Свободно
        HELD,      // Удерживается (те самые 10 минут)
        BOOKED,    // Выкуплено окончательно
        EXPIRED,   // Время удержания вышло
        CANCELLED  // Отменено
    }

