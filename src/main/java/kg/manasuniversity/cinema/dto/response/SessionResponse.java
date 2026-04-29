package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        String movieTitle,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price,
        String currency
) {}