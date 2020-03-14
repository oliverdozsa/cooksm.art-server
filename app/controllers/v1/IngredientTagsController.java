package controllers.v1;

import dto.IngredientTagDto;
import dto.PageDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import models.entities.IngredientTag;
import models.repositories.IngredientTagRepository;
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

public class IngredientTagsController extends Controller {
    @Inject
    private IngredientTagRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext executionContext;

    private static final Logger.ALogger logger = Logger.of(IngredientTagsController.class);

    public CompletionStage<Result> pageTags(Http.Request request) {
        Form<IngredientTagQueryParams> form =
                formFactory.form(IngredientTagQueryParams.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("pageTags(): form has errors!");
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            IngredientTagQueryParams queryParams = form.get();
            queryParams.limit = queryParams.limit == null ? 25 : queryParams.limit;
            queryParams.offset = queryParams.offset == null ? 0 : queryParams.offset;

            logger.info("pageTags(): queryParams = {}", queryParams);

            return repository.page(queryParams.nameLike, queryParams.languageId, queryParams.limit, queryParams.offset)
                    .thenApplyAsync(this::toResult, executionContext.current());
        }
    }

    @Constraints.Validate
    @Getter
    @Setter
    @ToString
    public static class IngredientTagQueryParams implements Constraints.Validatable<ValidationError> {
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

    private Result toResult(Page<IngredientTag> page) {
        List<IngredientTagDto> tags = page.getItems().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ok(toJson(new PageDto<>(tags, page.getTotalCount())));
    }
}
