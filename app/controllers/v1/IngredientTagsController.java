package controllers.v1;

import lombokized.dto.IngredientTagDto;
import lombokized.dto.PageDto;
import lombokized.queryparams.IngredientTagQueryParams;
import lombokized.repositories.Page;
import data.entities.IngredientTag;
import data.repositories.IngredientTagRepository;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.DtoMapper;

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
            queryParams.setLimit(queryParams.getLimit() == null ? 25 : queryParams.getLimit());
            queryParams.setOffset(queryParams.getOffset() == null ? 0 : queryParams.getOffset());

            logger.info("pageTags(): queryParams = {}", queryParams);

            return repository.page(queryParams.getNameLike(), queryParams.getLanguageId(), queryParams.getLimit(), queryParams.getOffset())
                    .thenApplyAsync(this::toResult, executionContext.current());
        }
    }

    private Result toResult(Page<IngredientTag> page) {
        List<IngredientTagDto> tags = page.getItems().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ok(toJson(new PageDto<>(tags, page.getTotalCount())));
    }
}
