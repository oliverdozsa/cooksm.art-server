package controllers.v1;

import dto.IngredientNameDto;
import models.entities.IngredientName;
import models.repositories.IngredientNameRepository;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;

public class IngredientNamesController extends Controller {
    @Inject
    private IngredientNameRepository ingredientNameRepository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext ec;

    public CompletionStage<Result> listNames(Http.Request request) {
        Form<IngredientNameQueryParams> form =
                formFactory.form(IngredientNameQueryParams.class).bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            IngredientNameQueryParams params = form.get();
            params.limit = params.limit == null ? 25 : params.limit;
            params.offset = params.offset == null ? 0 : params.offset;

            return ingredientNameRepository
                    .list(params.nameLike, params.languageId, params.limit, params.offset)
                    .thenApplyAsync(s -> ok(toJson(s.map(this::toDto).collect(Collectors.toList()))), ec.current());
        }
    }

    private IngredientNameDto toDto(IngredientName entity){
        return new IngredientNameDto(entity.getIngredient().getId(), entity.getName());
    }

    /**
     * Ingredient query parameters.
     */
    @Constraints.Validate
    public static class IngredientNameQueryParams implements Constraints.Validatable<ValidationError> {
        @Constraints.Required
        public Long languageId;

        @Constraints.MinLength(2)
        public String nameLike;

        @Constraints.Min(0)
        public Integer offset;

        @Constraints.Min(1)
        @Constraints.Max(50)
        public Integer limit;

        @Override
        public ValidationError validate() {
            return null;
        }
    }
}
