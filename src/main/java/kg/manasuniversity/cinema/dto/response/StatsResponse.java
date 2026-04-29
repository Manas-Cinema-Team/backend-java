package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record StatsResponse(
        long totalMovies,
        long totalSessions,
        long totalUsers,
        long totalBookings,
        long confirmedBookings,
        BigDecimal totalRevenue,
        List<SessionStatsResponse> topSessions
) {}