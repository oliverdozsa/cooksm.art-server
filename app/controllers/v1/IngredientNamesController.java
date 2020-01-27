package controllers.v1;

import dto.IngredientNameDto;
import dto.PageDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import models.entities.IngredientName;
import models.repositories.IngredientNameRepository;
import models.repositories.Page;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;

public class IngredientNamesController extends Controller {
    @Inject
    private IngredientNameRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext executionContext;

    private static final Logger.ALogger logger = Logger.of(IngredientNamesController.class);

    public CompletionStage<Result> pageNames(Http.Request request) {
        Form<IngredientNameQueryParams> form =
                formFactory.form(IngredientNameQueryParams.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("pageNames(): form has errors!");
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            IngredientNameQueryParams params = form.get();
            params.limit = params.limit == null ? 25 : params.limit;
            params.offset = params.offset == null ? 0 : params.offset;

            logger.info("pageNames(): params = {}", params);

            return repository.page(params.nameLike, params.languageId, params.limit, params.offset)
                    .thenApplyAsync(this::toResult);
        }
    }

    /**
     * Ingredient query parameters.
     */
    @Constraints.Validate
    @Getter
    @Setter
    @ToString
    public static class IngredientNameQueryParams implements Constraints.Validatable<ValidationError> {
        @Constraints.Required
        private Long languageId;

        @Constraints.MinLength(2)
        private String nameLike;

        @Constraints.Min(0)
        private Integer offset;

        @Constraints.Min(1)
        @Constraints.Max(50)
        private Integer limit = 25;

        @Override
        public ValidationError validate() {
            return null;
        }
    }

    private Result toResult(Page<IngredientName> page) {
        List<IngredientNameDto> dtos = page.getItems()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(dtos, page.getTotalCount())));
    }
}
