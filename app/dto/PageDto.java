package dto;


import lombok.Data;

import java.util.List;

@Data
public class PageDto <T> {
    private final List<T> items;
    private final Long totalCount;
}
