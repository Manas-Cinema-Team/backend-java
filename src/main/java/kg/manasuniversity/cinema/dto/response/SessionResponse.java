package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SessionResponse(
        Long id,
        MovieShortResponse movie,
        HallShortResponse hall,
        LocalDateTime startDatetime,
        LocalDateTime endDatetime,
        PriceResponse price,
        Boolean isActive,
        Integer availableSeats
) {}