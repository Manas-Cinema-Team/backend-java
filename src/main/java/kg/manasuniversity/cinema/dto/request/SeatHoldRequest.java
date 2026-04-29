package kg.manasuniversity.cinema.dto.request;

import java.util.List;

public record SeatHoldRequest(
        Long userId,
        Long sessionId,
        List<SelectedSeat> selectedSeats
) {
    public record SelectedSeat(int row, int number) {}
}