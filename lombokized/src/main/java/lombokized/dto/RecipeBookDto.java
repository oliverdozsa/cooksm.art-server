package lombokized.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class RecipeBookDto {
    private final String name;
    private final Instant lastAccessed;
}
