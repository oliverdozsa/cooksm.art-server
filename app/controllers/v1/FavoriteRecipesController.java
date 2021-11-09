package controllers.v1;

import com.typesafe.config.Config;
import data.DatabaseExecutionContext;
import lombokized.dto.FavoriteRecipeCreateDto;
import lombokized.dto.FavoriteRecipeDto;
import data.entities.FavoriteRecipe;
import data.repositories.FavoriteRecipeRepository;
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
import services.DtoMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class FavoriteRecipesController extends Controller {
    @Inject
    private Config config;

    @Inject
    private FavoriteRecipeRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(FavoriteRecipesController.class);

    public CompletionStage<Result> single(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("single(): id = {}, user id = ", id, jwt.getUserId());

        return supplyAsync(() -> {
            FavoriteRecipe favoriteRecipe = repository.single(id, jwt.getUserId());

            if (favoriteRecipe == null) {
                return notFound();
            }

            FavoriteRecipeDto dto = DtoMapper.toDto(favoriteRecipe);
            return ok(Json.toJson(dto));
        }, dbExecContext)
                .exceptionally(mapExceptionWithUnpack);

    }

    public CompletionStage<Result> all(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("allOfUser(): user id = {}", jwt.getUserId());
        return supplyAsync(() -> {
            List<FavoriteRecipe> favoriteRecipes = repository.all(jwt.getUserId());
            return toResult(favoriteRecipes);
        }, dbExecContext)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> create(Http.Request request) {
        Form<FavoriteRecipeCreateDto> form = formFactory.form(FavoriteRecipeCreateDto.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("create(): form has errors!");
            return supplyAsync(() -> badRequest(form.errorsAsJson()));
        } else {
            FavoriteRecipeCreateDto dto = form.get();
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
            logger.info("create(): user id = {}, dto = {}", jwt.getUserId(), dto);

            return supplyAsync(() -> {
                FavoriteRecipe favoriteRecipe = repository.create(jwt.getUserId(), dto.getRecipeId());
                String location = routes.FavoriteRecipesController
                        .single(favoriteRecipe.getId()).absoluteURL(request);
                return created().withHeader(LOCATION, location);
            }, dbExecContext)
                    .exceptionally(mapExceptionWithUnpack);
        }
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        logger.info("delete(): id = {}, user id = {}", id, jwt.getUserId());

        return supplyAsync(() -> {
            Boolean isDeleted = repository.delete(id, jwt.getUserId());
            if (isDeleted) {
                return (Result) noContent();
            } else {
                logger.warn("delete(): Failed to delete!");
                ValidationError ve = new ValidationError("", "Failed to delete!");
                return badRequest(Json.toJson(ve.messages()));
            }
        }, dbExecContext)
                .exceptionally(mapExceptionWithUnpack);
    }

    private static Result toResult(List<FavoriteRecipe> items) {
        List<FavoriteRecipeDto> dtoList = items
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());
        return ok(Json.toJson(dtoList));
    }
}
