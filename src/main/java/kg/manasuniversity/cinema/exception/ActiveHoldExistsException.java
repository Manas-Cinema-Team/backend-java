package kg.manasuniversity.cinema.exception;

public class ActiveHoldExistsException extends RuntimeException {
    private final Long bookingId;

    public ActiveHoldExistsException(Long bookingId) {
        super("ACTIVE_HOLD_EXISTS");
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }
}
