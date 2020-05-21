package controllers.v1;

import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http;
import queryparams.RecipesQueryParams;

public class RecipesQueryParamsRetrieve {
    private final FormFactory formFactory;
    private final Http.Request request;

    public RecipesQueryParamsRetrieve(FormFactory formFactory, Http.Request request) {
        this.formFactory = formFactory;
        this.request = request;
    }

    public Form<RecipesQueryParams.Params> retrieve() {
        Form<RecipesQueryParams.Params> searchModeForm = retrieveSearchModeForm();

        if (searchModeForm.hasErrors()) {
            return searchModeForm;
        } else {
            String searchModeStr = searchModeForm.get().searchMode;
            RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(searchModeStr);
            return retrieveRefinedForm(searchMode);
        }
    }

    private Form<RecipesQueryParams.Params> retrieveSearchModeForm() {
        // Get form without groups to access search mode.
        return formFactory.form(RecipesQueryParams.Params.class)
                .bindFromRequest(request);
    }

    private Form<RecipesQueryParams.Params> retrieveRefinedForm(RecipesQueryParams.SearchMode searchMode) {
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            return formFactory.form(RecipesQueryParams.Params.class, RecipesQueryParams.VGRecSearchModeComposedOf.class)
                    .bindFromRequest(request);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            return formFactory.form(RecipesQueryParams.Params.class, RecipesQueryParams.VGRecSearchModeComposedOfRatio.class)
                    .bindFromRequest(request);
        } else {
            return formFactory.form(RecipesQueryParams.Params.class).bindFromRequest(request);
        }
    }
}
