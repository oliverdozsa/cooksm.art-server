package controllers.v1;

import data.repositories.IngredientTagRepository;
import dto.IngredientTagCreateUpdateDto;
import lombokized.dto.IngredientTagDto;
import lombokized.dto.IngredientTagResolvedDto;
import lombokized.queryparams.IngredientTagQueryParams;
import lombokized.queryparams.IngredientTagsByIdsQueryParams;
import lombokized.repositories.Page;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.IngredientTagsService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;

public class IngredientTagsController extends Controller {
    @Inject
    private IngredientTagRepository repository;

    @Inject
    private IngredientTagsService service;

    @Inject
    private FormFactory formFactory;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(IngredientTagsController.class);

    public CompletionStage<Result> page(Http.Request request) {
        Form<IngredientTagQueryParams> form =
                formFactory.form(IngredientTagQueryParams.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("page(): form has errors! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        IngredientTagQueryParams queryParams = form.get();
        queryParams.setLimit(queryParams.getLimit() == null ? 25 : queryParams.getLimit());
        queryParams.setOffset(queryParams.getOffset() == null ? 0 : queryParams.getOffset());
        logger.info("page(): queryParams = {}", queryParams);

        if (SecurityUtils.hasVerifiedJwt(request)) {
            logger.info("page(): request is authenticated.");
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
            return service.page(queryParams, jwt.getUserId())
                    .thenApplyAsync(this::toResult);

        } else {
            logger.info("page(): request is unauthenticated.");
            return service.page(queryParams)
                    .thenApplyAsync(this::toResult);
        }
    }

    public CompletionStage<Result> create(Http.Request request) {
        Form<IngredientTagCreateUpdateDto> form = formFactory.form(IngredientTagCreateUpdateDto.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("create(): form has error! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        IngredientTagCreateUpdateDto dto = form.get();

        logger.info("create(): dto = {}", dto);

        return service.create(dto, jwt.getUserId())
                .thenApplyAsync(id -> {
                    String location = routes.IngredientTagsController.single(id, 0L).absoluteURL(request);
                    logger.info("create(): location = {}", location);
                    return created().withHeader(LOCATION, location);
                })
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> single(Long id, Long languageId, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        logger.info("single(): id = {}, languageId = {}, userId = ", id, languageId, jwt.getUserId());

        return service.single(id, languageId, jwt.getUserId())
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> update(Long id, Http.Request request) {
        Form<IngredientTagCreateUpdateDto> form = formFactory.form(IngredientTagCreateUpdateDto.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("update(): form has errors! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        IngredientTagCreateUpdateDto dto = form.get();

        logger.info("update(): id = {}, userId = {}", id, jwt.getUserId());

        return service.update(id, dto, jwt.getUserId())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        logger.info("delete(): id = {}, userId = {}", id, jwt.getUserId());

        return service.delete(id, jwt.getUserId())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> userDefined(Long languageId, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        logger.info("userDefined(): userId = {}", jwt.getUserId());

        return service.userDefined(jwt.getUserId(), languageId)
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> byIds(Http.Request request) {
        Form<IngredientTagsByIdsQueryParams> form = formFactory.form(IngredientTagsByIdsQueryParams.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("byIds(): form has errors! errors = {}", form.errorsAsJson().toPrettyString());
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        IngredientTagsByIdsQueryParams queryParams = form.get();

        logger.info("byIds(): queryParams = {}", queryParams);

        return service.byIds(queryParams.getLanguageId(), queryParams.getTagIds())
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    private Result toResult(Page<IngredientTagDto> pageDto) {
        return ok(toJson(pageDto));
    }

    private Result toResult(IngredientTagResolvedDto dto) {
        return ok(toJson(dto));
    }

    private Result toResult(List<IngredientTagDto> tagDtos) {
        return ok(toJson(tagDtos));
    }
}
