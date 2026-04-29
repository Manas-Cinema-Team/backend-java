package kg.manasuniversity.cinema.dto.response;

import java.time.Instant;
import java.util.List;

public record SeatHoldResponse(
        Long sessionId,
        Instant expiresAt, // Единое время для всей группы мест
        List<HeldSeat> heldSeats
) {
    public record HeldSeat(int row, int number) {}
}