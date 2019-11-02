package models.repositories.imp;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.v1.RecipesControllerQuery;
import dto.RecipeSearchCreateUpdateDto;
import models.repositories.exceptions.BusinessLogicViolationException;
import play.libs.Json;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collections;
import java.util.Set;

public class RecipeSearchCreateUpdateDtoValidator {
    private Validator validator;

    public RecipeSearchCreateUpdateDtoValidator(Validator validator) {
        this.validator = validator;
    }

    public void validate(RecipeSearchCreateUpdateDto dto){
        validateQuery(dto.getQuery());
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
