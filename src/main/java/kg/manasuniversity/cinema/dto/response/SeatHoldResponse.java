package kg.manasuniversity.cinema.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record SeatHoldResponse(
        Long sessionId,
        LocalDateTime expiresAt, // Единое время для всей группы мест
        List<HeldSeat> heldSeats
) {
    public record HeldSeat(int row, int number) {}
}