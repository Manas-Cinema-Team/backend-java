package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;

public record BookingSeatResponse(
        Integer row,
        Integer number,
        String type,
        BigDecimal priceAtBooking
) {}