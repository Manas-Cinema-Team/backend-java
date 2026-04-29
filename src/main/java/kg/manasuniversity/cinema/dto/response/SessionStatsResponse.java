package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;

public record SessionStatsResponse(
        Long sessionId,
        String movieTitle,
        String hallName,
        long totalBookings,
        long confirmedBookings,
        BigDecimal revenue
) {}