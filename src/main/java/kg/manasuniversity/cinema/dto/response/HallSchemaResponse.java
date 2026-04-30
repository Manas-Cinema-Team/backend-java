package kg.manasuniversity.cinema.dto.response;

import java.util.List;

public record HallSchemaResponse(
        List<Row> rows,
        List<SeatMeta> seats,
        List<Coordinate> disabledSeats
) {
    public record Row(int row, List<RowSeat> seats) {}
    public record RowSeat(int number, String type) {}
    public record SeatMeta(int row, int number, String type) {}
    public record Coordinate(int row, int number) {}
}
