package queryparams;

import com.fasterxml.jackson.annotation.JsonInclude;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.List;

public class RecipesQueryParams {
    public enum SearchMode {
        NONE,
        COMPOSED_OF_NUMBER,
        COMPOSED_OF_RATIO
    }

    @Constraints.Validate
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Params implements Constraints.Validatable<ValidationError> {
        @Constraints.Pattern("(composed-of-number|composed-of-ratio)")
        public String searchMode;

        @Constraints.Min(0)
        public Integer minIngs;

        @Constraints.Min(0)
        public Integer maxIngs;

        @Constraints.Pattern("(name|numofings)")
        public String orderBy;

        @Constraints.Pattern("(asc|desc)")
        public String orderBySort;

        @Constraints.Required(groups = {VGRecSearchModeComposedOf.class})
        @Constraints.Min(0)
        public Integer unknownIngs;

        @Constraints.Pattern("(eq|gt|lt|ge|le)")
        @Constraints.Required(groups = {VGRecSearchModeComposedOf.class})
        public String unknownIngsRel;

        @Constraints.Required(groups = {VGRecSearchModeComposedOf.class})
        @Constraints.Min(0)
        public Integer goodIngs;

        @Constraints.Min(0)
        public Integer goodAdditionalIngs;

        @Constraints.Pattern("(eq|gt|lt|ge|le)")
        @Constraints.Required(groups = {VGRecSearchModeComposedOf.class})
        public String goodIngsRel;

        @Constraints.Min(0)
        public Integer offset;

        @Constraints.Min(1)
        @Constraints.Max(50)
        public Integer limit;

        // Must be between 0.0, and 1.0. Check by validate()
        @Constraints.Required(groups = {VGRecSearchModeComposedOfRatio.class})
        public Float goodIngsRatio;

        public String nameLike;

        public List<Long> exIngs;

        public List<Long> exIngTags;

        public List<Long> inIngs;

        public List<Long> inIngTags;

        public List<Long> addIngs;

        public List<Long> addIngTags;

        public List<Long> sourcePages;

        public Long languageId;

        public Boolean useFavoritesOnly;

        @Override
        public ValidationError validate() {
            SearchMode searchModeEnum = null;
            try {
                searchModeEnum = toEnum(searchMode);
            } catch (IllegalArgumentException e) {
                return new ValidationError("", "Invalid search mode!");
            }

            if (searchModeEnum != null && searchModeEnum != SearchMode.NONE) {
                return checkSearchModeRelatedParams(searchModeEnum);
            }

            return null;
        }

        public static SearchMode toEnum(String searchModeStr) {
            if (searchModeStr == null) {
                return SearchMode.NONE;
            }

            String transformed = searchModeStr
                    .replace("-", "_")
                    .toUpperCase();
            return SearchMode.valueOf(transformed);
        }

        @Override
        public String toString() {
            return "Params{" +
                    "searchMode='" + searchMode + '\'' +
                    ", minIngs=" + minIngs +
                    ", maxIngs=" + maxIngs +
                    ", orderBy='" + orderBy + '\'' +
                    ", orderBySort='" + orderBySort + '\'' +
                    ", unknownIngs=" + unknownIngs +
                    ", unknownIngsRel='" + unknownIngsRel + '\'' +
                    ", goodIngs=" + goodIngs +
                    ", goodAdditionalIngs=" + goodAdditionalIngs +
                    ", goodIngsRel='" + goodIngsRel + '\'' +
                    ", offset=" + offset +
                    ", limit=" + limit +
                    ", goodIngsRatio=" + goodIngsRatio +
                    ", nameLike='" + nameLike + '\'' +
                    ", exIngs=" + exIngs +
                    ", exIngTags=" + exIngTags +
                    ", inIngs=" + inIngs +
                    ", inIngTags=" + inIngTags +
                    ", addIngs=" + addIngs +
                    ", addIngTags=" + addIngTags +
                    ", sourcePages=" + sourcePages +
                    ", languageId=" + languageId +
                    ", useFavoritesOnly=" + useFavoritesOnly +
                    '}';
        }

        private ValidationError checkSearchModeRelatedParams(SearchMode searchMode) {
            if (inIngs == null && inIngTags == null) {
                return new ValidationError("", "Missing input ingredients (no tags, or ingredients)!");
            } else if (searchMode == SearchMode.COMPOSED_OF_RATIO) {
                return checkGoodIngsRatio();
            } else if (searchMode == SearchMode.COMPOSED_OF_NUMBER) {
                return checkAdditionaIngs();
            }

            return null;
        }

        private ValidationError checkGoodIngsRatio() {
            if (goodIngsRatio < 0.0 || goodIngsRatio > 1.0) {
                return new ValidationError("", "Invalid good ingredients ratio! Must be between 0.0 and 1.0!");
            }

            return null;
        }

        private ValidationError checkAdditionaIngs() {
            if (goodAdditionalIngs == null) {
                return null;
            }

            if (goodAdditionalIngs <= 0) {
                return new ValidationError("", "Invalid good additional ingredients number!");
            }

            if (addIngTags == null && addIngs == null) {
                return new ValidationError("", "Missing input additional ingredients (no tags, or ingredients)!");
            }

            return null;
        }
    }

    // Validation group for ratio mode search.
    public interface VGRecSearchModeComposedOfRatio {
    }

    // Validation group for composed of search
    public interface VGRecSearchModeComposedOf {
    }
}
