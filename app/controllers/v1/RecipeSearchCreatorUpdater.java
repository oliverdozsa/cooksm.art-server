package controllers.v1;

import dto.RecipeSearchCreateUpdateDto;
import models.repositories.exceptions.BusinessLogicViolationException;
import play.api.libs.json.JsValue;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Lang;
import play.libs.Json;
import play.libs.typedmap.TypedMap;
import play.mvc.Http;

import java.util.Map;

/* Instances of this class is used only inside the appropriate controller in order to avoid
 * leaking the managed form factory.
 */
class RecipeSearchCreatorUpdater {
    private FormFactory formFactory;
    private RecipeSearchCreateUpdateDto dto;

    public RecipeSearchCreatorUpdater(FormFactory formFactory, Http.Request request) {
        this.formFactory = formFactory;
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
        Form<RecipesControllerQuery.Params> form = formFactory.form(RecipesControllerQuery.Params.class);
        form.bind(Lang.defaultLang().asJava(), TypedMap.empty(), Json.parse(query));

        if (form.hasErrors()) {
            throw new BusinessLogicViolationException("Recipe search to create is not valid!");
        }

        Integer searchMode = form.get().searchMode;
        validateQueryBasedOnSearchMode(query, searchMode);
    }

    private void validateQueryBasedOnSearchMode(String query, Integer searchmode) {
        if (searchmode == null || searchmode == RecipesControllerQuery.SearchMode.NONE.id) {
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
