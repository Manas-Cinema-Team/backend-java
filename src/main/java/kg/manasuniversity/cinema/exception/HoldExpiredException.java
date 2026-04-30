package kg.manasuniversity.cinema.exception;

public class HoldExpiredException extends RuntimeException {
    public HoldExpiredException() {
        super("HOLD_EXPIRED");
    }
}
