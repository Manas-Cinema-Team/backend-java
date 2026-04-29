package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;

public record PriceResponse(
        BigDecimal amount,
        String currency
) {}