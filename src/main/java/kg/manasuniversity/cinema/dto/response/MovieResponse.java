package kg.manasuniversity.cinema.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MovieResponse(
        Long id,
        String title,
        String description,
        String genre,
        Integer duration,
        String ageRating,
        String posterUrl,
        LocalDate releaseDate,
        Boolean isActive,
        List<SessionResponse> sessions
) {}