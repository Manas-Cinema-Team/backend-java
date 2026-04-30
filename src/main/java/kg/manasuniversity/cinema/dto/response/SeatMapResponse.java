package kg.manasuniversity.cinema.dto.response;

import java.time.Instant;
import java.util.List;

public record SeatMapResponse(
        Long hallId,
        String hallName,
        HallSchemaResponse schema,
        List<SeatResponse> seats,
        Integer pollingInterval,
        Instant serverTime,
        Integer availableSeats
) {}
