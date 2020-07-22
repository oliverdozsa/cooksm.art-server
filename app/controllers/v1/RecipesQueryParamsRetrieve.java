package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.i18n.MessagesApi;
import play.mvc.Http;
import queryparams.RecipesQueryParams;
import security.SecurityUtils;

class RecipesQueryParamsRetrieve {
    private final FormFactory formFactory;
    private final Http.Request request;
    private MessagesApi messagesApi;
    private JsonNode data;

    public RecipesQueryParamsRetrieve(FormFactory formFactory, Http.Request request) {
        this.formFactory = formFactory;
        this.request = request;
    }

    public RecipesQueryParamsRetrieve(FormFactory formFactory, MessagesApi messagesApi, Http.Request request, JsonNode data) {
        this(formFactory, request);
        this.messagesApi = messagesApi;
        this.data = data;
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
        Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class);
        if (isCustomRetrieving()) {
            return form.bind(messagesApi.preferred(request).lang(), request.attrs(), data);
        } else {
            return form.bindFromRequest(request);
        }
    }

    private Form<RecipesQueryParams.Params> retrieveRefinedForm(RecipesQueryParams.SearchMode searchMode) {
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            return bind(RecipesQueryParams.VGRecSearchModeComposedOf.class);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            return bind(RecipesQueryParams.VGRecSearchModeComposedOfRatio.class);
        } else {
            return bind();
        }
    }

    private Form<RecipesQueryParams.Params> bind(Class<?> validationGroup) {
        Form<RecipesQueryParams.Params> form = getBaseForm(validationGroup);

        if (isCustomRetrieving()) {
            form = form.bind(messagesApi.preferred(request).lang(), request.attrs(), data);
        } else {
            form = form.bindFromRequest(request);
        }

        if (!form.hasErrors() && isUseFavoritesOnlyInvalid(form.get())) {
            form = form.withError(new ValidationError("", "Favorites only is set, but JWT is not present!"));
        }

        return form;
    }

    private boolean isCustomRetrieving() {
        return messagesApi != null;
    }

    private Form<RecipesQueryParams.Params> bind() {
        return bind(null);
    }

    private Form<RecipesQueryParams.Params> getBaseForm(Class<?> validationGroup) {
        if (validationGroup != null) {
            return formFactory.form(RecipesQueryParams.Params.class, validationGroup);
        } else {
            return formFactory.form(RecipesQueryParams.Params.class);
        }
    }

    private boolean isUseFavoritesOnlyInvalid(RecipesQueryParams.Params params) {
        return Boolean.TRUE.equals(params.useFavoritesOnly) && hasNoVerifiedJwt();
    }

    private boolean hasNoVerifiedJwt() {
        return !SecurityUtils.hasVerifiedJwt(request);
    }
}
