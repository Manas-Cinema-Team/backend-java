package kg.manasuniversity.cinema.dto.request;

import java.time.LocalDate;

public record MovieCreateRequest(
        String title,
        String description,
        String genre,
        Integer duration,
        String ageRating,
        String posterUrl,
        LocalDate releaseDate,
        Boolean isActive
) {
}
