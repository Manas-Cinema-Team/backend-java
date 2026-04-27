package kg.manasuniversity.cinema.dto.request;

import java.util.List;

public record BookingRequest(
        Long sessionId,
        List<SeatRequest> seats
) {
    public record SeatRequest(Integer row, Integer number) {}
}