package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import dto.RecipeSearchCreateUpdateDto;
import models.repositories.exceptions.BusinessLogicViolationException;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Http;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Set;

/* Instances of this class is used only inside the appropriate controller in order to avoid
 * leaking the managed form factory.
 */
class RecipeSearchCreatorUpdater {
    private RecipeSearchCreateUpdateDto dto;
    private Validator validator;

    public RecipeSearchCreatorUpdater(FormFactory formFactory, Http.Request request, Validator validator) {
        this.validator = validator;
        Form<RecipeSearchCreateUpdateDto> form = formFactory.form(RecipeSearchCreateUpdateDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            throw new BusinessLogicViolationException(form.errorsAsJson().toString());
        }

        dto = form.get();
    }

    public RecipeSearchCreateUpdateDto create() {
        validateQuery(dto.getQuery());
        return dto;
    }

    private void validateQuery(String query) {
        JsonNode queryJson = Json.parse(query);
        RecipesControllerQuery.Params params = Json.fromJson(queryJson, RecipesControllerQuery.Params.class);
        Set<ConstraintViolation<RecipesControllerQuery.Params>> violations = validator.validate(params);

        if (violations.size() > 0) {
            throw new BusinessLogicViolationException("Recipe search to create is not valid!");
        }

        validateQueryBasedOnSearchMode(params);
    }

    private void validateQueryBasedOnSearchMode(RecipesControllerQuery.Params params) {
        if (params.searchMode == null || params.searchMode == RecipesControllerQuery.SearchMode.NONE.id) {
            return;
        }

        Set<ConstraintViolation<RecipesControllerQuery.Params>> violations = Collections.EMPTY_SET;

        if (params.searchMode == RecipesControllerQuery.SearchMode.COMPOSED_OF.id) {
            violations = validator.validate(params, RecipesControllerQuery.VGRecSearchModeComposedOf.class);
        } else if (params.searchMode == RecipesControllerQuery.SearchMode.COMPOSED_OF_RATIO.id) {
            violations = validator.validate(params, RecipesControllerQuery.VGRecSearchModeComposedOfRatio.class);
        } else {
            throw new RuntimeException("Unknown searchmode!");
        }

        if (violations.size() > 0) {
            throw new BusinessLogicViolationException("Recipe search to create based on search mode is not valid!");
        }
    }
}
