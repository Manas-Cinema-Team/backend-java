package kg.manasuniversity.cinema.dto.request;

import java.util.List;

public record BookingRequest(
        Long userId,
        Long sessionId,
        List<Long> seatHoldIds
) {}