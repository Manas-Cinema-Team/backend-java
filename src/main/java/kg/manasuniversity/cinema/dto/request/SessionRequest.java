package kg.manasuniversity.cinema.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionRequest(
        Long movieId,
        Long hallId,
        Instant startTime,
        BigDecimal price
) {}