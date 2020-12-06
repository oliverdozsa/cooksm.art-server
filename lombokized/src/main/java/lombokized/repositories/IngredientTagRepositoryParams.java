package lombokized.repositories;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@ToString
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
