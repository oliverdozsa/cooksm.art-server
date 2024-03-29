package controllers.v1;

import data.DatabaseExecutionContext;
import lombokized.dto.IngredientNameDto;
import lombokized.dto.PageDto;
import lombokized.queryparams.IngredientNameByIngredientIdsQueryParams;
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
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.libs.Json.toJson;

public class IngredientNamesController extends Controller {
    @Inject
    private IngredientNameRepository repository;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    @Inject
    private FormFactory formFactory;

    private static final Logger.ALogger logger = Logger.of(IngredientNamesController.class);

    public CompletionStage<Result> pageNames(Http.Request request) {
        Form<IngredientNameQueryParams> form =
                formFactory.form(IngredientNameQueryParams.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("pageNames(): form has errors! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            IngredientNameQueryParams params = form.get();
            params.setLimit(params.getLimit() == null ? 25 : params.getLimit());
            params.setOffset(params.getOffset() == null ? 0 : params.getOffset());

            logger.info("pageNames(): params = {}", params);

            return supplyAsync(() -> {
                Page<IngredientName> ingredientNamePage = repository.page(params.getNameLike(), params.getLanguageId(), params.getLimit(), params.getOffset());
                return toResult(ingredientNamePage);
            }, dbExecContext);
        }
    }

    public CompletionStage<Result> byIngredientIds(Http.Request request) {
        Form<IngredientNameByIngredientIdsQueryParams> form = formFactory.form(IngredientNameByIngredientIdsQueryParams.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("byIngredientIds(): form has errors! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            IngredientNameByIngredientIdsQueryParams queryParams = form.get();
            logger.info("byIngredientIds(): queryParams = {}", queryParams);
            return supplyAsync(() -> {
                List<IngredientName> entities = repository.byIngredientIds(queryParams.getIngredientIds(), queryParams.getLanguageId());
                return toResult(entities);
            }, dbExecContext);
        }
    }

    private Result toResult(Page<IngredientName> page) {
        List<IngredientNameDto> dtos = page.getItems()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(dtos, page.getTotalCount())));
    }

    private Result toResult(List<IngredientName> ingredientNames) {
        List<IngredientNameDto> dtos = ingredientNames.stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return ok(toJson(dtos));
    }
}
