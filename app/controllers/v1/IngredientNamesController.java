package controllers.v1;

import lombokized.dto.IngredientNameDto;
import lombokized.dto.PageDto;
import lombokized.queryparams.IngredientNameQueryParams;
import data.entities.IngredientName;
import data.repositories.IngredientNameRepository;
import lombokized.repositories.Page;
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
            params.setLimit(params.getLimit() == null ? 25 : params.getLimit());
            params.setOffset(params.getOffset() == null ? 0 : params.getOffset());

            logger.info("pageNames(): params = {}", params);

            return repository.page(params.getNameLike(), params.getLanguageId(), params.getLimit(), params.getOffset())
                    .thenApplyAsync(this::toResult, executionContext.current());
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
