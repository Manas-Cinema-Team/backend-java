package kg.manasuniversity.cinema.dto.response;

import java.util.List;

public record SeatMapResponse(
        Long hallId,
        String hallName,
        List<SeatResponse> seats,
        Integer pollingInterval
) {}