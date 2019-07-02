package controllers.v1;

import lombok.Getter;
import lombok.Setter;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import javax.validation.groups.Default;
import java.util.List;

class RecipesControllerQuery {
    public enum SearchMode {
        NONE(0),
        COMPOSED_OF(1),
        GROUP(2),
        COMPOSED_OF_RATIO(3);

        SearchMode(int id) {
            this.id = id;
        }

        public int id;

        public static SearchMode getByIntVal(int val) {
            SearchMode result = NONE;

            SearchMode[] options = SearchMode.class.getEnumConstants();
            for (SearchMode field : options) {
                if (field.id == val) {
                    result = field;
                    break;
                }
            }

            return result;
        }
    }

    @Constraints.Validate
    public static class Params implements Constraints.Validatable<ValidationError> {
        @Constraints.Min(1)
        @Constraints.Max(3)
        public Integer searchMode;

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

        public List<Long> sourcePages;

        @Constraints.Required(groups = {VGRecSearchModeGroup.class})
        public List<List<Long>> inGrOfIngs;

        @Constraints.Required(groups = {VGRecSearchModeComposedOf.class, VGRecSearchModeComposedOfRatio.class, VGRecSearchModeGroup.class})
        public Long languageId;

        @Override
        public ValidationError validate() {
            ValidationError result = null;

            if (searchMode != null) {
                if (searchMode == SearchMode.COMPOSED_OF.id ||
                        searchMode == SearchMode.COMPOSED_OF_RATIO.id) {
                    if (inIngs == null && inIngTags == null) {
                        result = new ValidationError("", "Missing input ingredients (no tags, or ingredients)!");
                    } else if (searchMode == SearchMode.COMPOSED_OF_RATIO.id) {
                        if (goodIngsRatio < 0.0 || goodIngsRatio > 1.0) {
                            result = new ValidationError("", "Invalid good ingredients ratio! Must be between 0.0 and 1.0!");
                        }
                    }
                }
            }

            return result;
        }
    }

    // Validation group for ratio mode search.
    public interface VGRecSearchModeComposedOfRatio {
    }

    // Validation group for composed of search
    public interface VGRecSearchModeComposedOf {
    }

    public interface VGRecSearchModeGroup {
    }
}
