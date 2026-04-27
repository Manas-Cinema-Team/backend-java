package kg.manasuniversity.cinema.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {
    private long count;
    private String next;
    private String previous;
    private List<T> results;

    public PageResponse(long count, String next, String previous, List<T> results) {
        this.count = count;
        this.next = next;
        this.previous = previous;
        this.results = results;
    }
}