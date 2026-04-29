package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;

public record SeatResponse(
        Integer row,
        Integer number,
        String type,
        String status,
        Boolean heldByMe,
        String expiresAt,
        PriceResponse price
) {}