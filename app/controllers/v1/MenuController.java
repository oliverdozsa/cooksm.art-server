package controllers.v1;

import data.entities.Menu;
import dto.MenuCreateUpdateDto;
import lombokized.dto.MenuDto;
import lombokized.dto.MenuTitleDto;
import org.slf4j.LoggerFactory;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.VerifiedJwt;
import services.MenuService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static security.SecurityUtils.getFromRequest;

public class MenuController extends Controller {
    @Inject
    private FormFactory formFactory;

    @Inject
    private MenuService service;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(MenuController.class);

    public CompletionStage<Result> create(Http.Request request) {
        logger.info("create()");

        Form<MenuCreateUpdateDto> form = formFactory.form(MenuCreateUpdateDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = getFromRequest(request);

        return service.create(form.get(), jwt.getUserId())
                .thenApplyAsync(id -> {
                    String location = routes.MenuController.getById(id, 0L).absoluteURL(request);
                    logger.info("create(): created menu with id = {}", id);
                    return created().withHeader(LOCATION, location);
                })
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        logger.info("delete(): id = {}", id);

        VerifiedJwt jwt = getFromRequest(request);

        return service.delete(id, jwt.getUserId())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> update(Long id, Http.Request request) {
        logger.info("update(): id = {}", id);

        Form<MenuCreateUpdateDto> form = formFactory.form(MenuCreateUpdateDto.class).bindFromRequest(request);

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        VerifiedJwt jwt = getFromRequest(request);

        return service.update(id, form.get(), jwt.getUserId())
                .thenApplyAsync(v -> (Result) noContent())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> getById(Long id, Long languageId, Http.Request request) {
        logger.info("getById(): id = {}, languageId = {}", id);

        VerifiedJwt jwt = getFromRequest(request);
        return service.single(id, languageId, jwt.getUserId())
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> getAll(Http.Request request) {
        logger.info("getAll()");

        VerifiedJwt jwt = getFromRequest(request);
        return service.all(jwt.getUserId())
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    private Result toResult(MenuDto menuDto) {
        return ok(Json.toJson(menuDto));
    }

    private Result toResult(List<MenuTitleDto> menuTitles) {
        return ok(Json.toJson(menuTitles));
    }
}
