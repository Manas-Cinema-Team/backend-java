package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record SeatResponse(
        Integer row,
        Integer number,
        String type,
        String status,
        Boolean heldByMe,
        Instant expiresAt,
        PriceResponse price
) {}