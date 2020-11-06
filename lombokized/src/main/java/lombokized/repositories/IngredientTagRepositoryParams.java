package lombokized.repositories;

import lombok.Builder;
import lombok.Value;

public class IngredientTagRepositoryParams {
    @Value
    @Builder(builderClassName = "Builder", toBuilder = true)
    public static class Page {
        public String nameLike;
        public Long languageId;
        public Long userId;
        public int limit;
        public int offset;
    }
}
