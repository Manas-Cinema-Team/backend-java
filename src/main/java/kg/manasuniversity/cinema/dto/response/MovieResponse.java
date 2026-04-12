package kg.manasuniversity.cinema.dto.response;

public record MovieResponse(
        Long id,
        String name,
        String description,
        String genre,
        Integer duration,
        String ageRating,
        String posterUrl,
        java.time.LocalDate releaseDate,
        Boolean isActive) {
}
