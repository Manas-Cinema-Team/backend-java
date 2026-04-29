package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record SessionResponse(
        Long id,
        MovieShortResponse movie,
        HallShortResponse hall,
        Instant startDatetime,
        Instant endDatetime,
        PriceResponse price,
        Boolean isActive,
        Integer availableSeats
) {}