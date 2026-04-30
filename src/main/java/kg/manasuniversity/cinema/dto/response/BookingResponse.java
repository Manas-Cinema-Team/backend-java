package kg.manasuniversity.cinema.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        SessionShortResponse session,
        List<BookingSeatResponse> seats,
        BigDecimal totalAmount,
        String currency,
        String bookingStatus,
        String paymentStatus,
        Instant expiresAt,
        Instant serverTime,
        Instant confirmedAt,
        Instant createdAt
) {
    public record SessionShortResponse(
            Long id,
            MovieRef movie,
            HallRef hall,
            Instant startDatetime
    ) {}

    public record MovieRef(String title) {}

    public record HallRef(String name) {}
}
