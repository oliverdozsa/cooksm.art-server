package controllers.v1;

import dto.RecipeSearchCreateUpdateDto;
import models.repositories.exceptions.BusinessLogicViolationException;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;

public class RecipeSearchCreatorUpdater {
    private FormFactory formFactory;

    public RecipeSearchCreatorUpdater(FormFactory formFactory) {
        this.formFactory = formFactory;
    }

    public RecipeSearchCreateUpdateDto create(String name, String query) {
        validateName(name);
        validateQuery(query);

        return new RecipeSearchCreateUpdateDto(name, query);
    }

    private void validateName(String name) {
        if (name == null || name.length() < 2) {
            throw new BusinessLogicViolationException("Recipe search's name is invalid!");
        }
    }

    private void validateQuery(String query) {
        Form<RecipesControllerQuery.Params> form = formFactory.form(RecipesControllerQuery.Params.class);
        form.bind(null, null, Json.parse(query));

        if (form.hasErrors()) {
            throw new BusinessLogicViolationException("Recipe search to create is not valid!");
        }

        Integer searchMode = form.get().searchMode;
        validateQueryBasedOnSearchMode(query, searchMode);
    }

    private void validateQueryBasedOnSearchMode(String query, Integer searchmode) {
        if (searchmode == RecipesControllerQuery.SearchMode.NONE.id) {
            return;
        }

        Form<RecipesControllerQuery.Params> form;
        if (searchmode == RecipesControllerQuery.SearchMode.COMPOSED_OF.id) {
            form = formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOf.class);
        } else if (searchmode == RecipesControllerQuery.SearchMode.COMPOSED_OF_RATIO.id) {
            form = formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOfRatio.class);
        } else {
            throw new RuntimeException("Unknown searchmode!");
        }

        form.bind(null, null, Json.parse(query));

        if (form.hasErrors()) {
            throw new BusinessLogicViolationException("Recipe search to create based on search mode is not valid!");
        }
    }
}
