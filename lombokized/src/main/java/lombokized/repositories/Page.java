package lombokized.repositories;

import lombok.Data;

import java.util.List;

@Data
public class Page <T> {
    private final List<T> items;
    private final int totalCount;
}
