package lombokized.dto;

import lombok.Data;

@Data
public class SourcePageDto {
    private final Long id;
    private final String name;
    private final String language;
}
