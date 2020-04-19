package controllers.v1;

import com.typesafe.config.Config;
import lombokized.dto.PageDto;
import lombokized.dto.RecipeSearchCreateUpdateDto;
import lombokized.dto.RecipeSearchDto;
import models.repositories.RecipeSearchRepository;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class RecipeSearchesController extends Controller {
    @Inject
    private RecipeSearchRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private Config config;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());


    private static final Logger.ALogger logger = Logger.of(RecipeSearchesController.class);

    public CompletionStage<Result> globals() {
        logger.info("globals()");
        return repository.globals().thenApplyAsync(p -> {
            List<RecipeSearchDto> searchDtos = p.getItems().stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());

            return ok(Json.toJson(new PageDto<>(searchDtos, p.getTotalCount())));
        }, httpExecutionContext.current());
    }

    public CompletionStage<Result> userSearches(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("userSearches(): user id = {}", jwt.getUserId());
        return repository.userSearches(jwt.getUserId())
                .thenApplyAsync(p -> {
                    List<RecipeSearchDto> searchDtos = p.getItems().stream()
                            .map(DtoMapper::toDto)
                            .collect(Collectors.toList());

                    return ok(Json.toJson(new PageDto<>(searchDtos, p.getTotalCount())));
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> userSearch(Long entityId, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("userSearch(): entityId = {}, user id = {}", entityId, jwt.getUserId());
        return repository.userSearch(jwt.getUserId(), entityId)
                .thenApplyAsync(e -> {
                    if (e == null) {
                        return notFound();
                    }

                    RecipeSearchDto dto = DtoMapper.toDto(e);
                    return ok(Json.toJson(dto));
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> create(Http.Request request) {
        Form<RecipeSearchCreateUpdateDto> form = formFactory.form(RecipeSearchCreateUpdateDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            logger.warn("create(): Form has errors!");
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        RecipeSearchCreateUpdateDto dto = form.get();
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("create(): user id = {}, dto = {}", jwt.getUserId(), dto);
        return repository.create(jwt.getUserId(), dto)
                .thenApplyAsync(id -> {
                    String location = routes.RecipeSearchesController.userSearch(id).absoluteURL(request);
                    return created().withHeader(LOCATION, location);
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> update(Long id, Http.Request request) {
        Form<RecipeSearchCreateUpdateDto> form = formFactory.form(RecipeSearchCreateUpdateDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            logger.warn("update(): Form has errors!");
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        RecipeSearchCreateUpdateDto dto = form.get();
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("update(): user id = {}, id = {}, dto = {}", jwt.getUserId(), id, dto);
        return repository.update(jwt.getUserId(), id, dto)
                .thenApplyAsync(v -> (Result) noContent(), httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("delete(): id = {}, user id = {}", id, jwt.getUserId());
        return repository.delete(jwt.getUserId(), id)
                .thenApplyAsync(success -> {
                    if (success) {
                        return (Result) noContent();
                    } else {
                        logger.warn("delete(): failed to delete!");
                        ValidationError ve = new ValidationError("", "Failed to delete.");
                        return badRequest(Json.toJson(ve.messages()));
                    }
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }
}
