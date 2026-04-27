package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        String movieTitle,      // Добавили для красоты в чеке
        LocalDateTime sessionTime,
        BigDecimal totalAmount,
        String status,
        LocalDateTime confirmedAt,
        List<SeatInfo> seats    // Тот самый список мест
) {
    // Вложенный рекорд для компактности
    public record SeatInfo(int row, int number, BigDecimal price) {}
}