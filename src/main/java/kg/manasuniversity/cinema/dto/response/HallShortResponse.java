package kg.manasuniversity.cinema.dto.response;

public record HallShortResponse(
        Long id,
        String name,
        Integer rows,
        Integer seatsPerRow
) {}