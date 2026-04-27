package kg.manasuniversity.cinema.dto.response;

public record MovieShortResponse(
        Long id,
        String title,
        String posterUrl,
        Integer duration,
        String ageRating
) {}