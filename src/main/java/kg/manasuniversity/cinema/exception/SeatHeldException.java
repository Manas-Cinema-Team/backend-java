package kg.manasuniversity.cinema.exception;

import kg.manasuniversity.cinema.dto.request.BookingRequest;
import java.util.List;

public class SeatHeldException extends RuntimeException {
    private final List<BookingRequest.SeatRequest> conflictSeats;

    public SeatHeldException(List<BookingRequest.SeatRequest> conflictSeats) {
        super("SEAT_HELD");
        this.conflictSeats = conflictSeats;
    }

    public List<BookingRequest.SeatRequest> getConflictSeats() {
        return conflictSeats;
    }
}