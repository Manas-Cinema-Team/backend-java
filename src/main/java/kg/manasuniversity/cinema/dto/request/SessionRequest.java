package kg.manasuniversity.cinema.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionRequest(
        Long movieId,
        Long hallId,
        LocalDateTime startTime,
        BigDecimal price
) {}