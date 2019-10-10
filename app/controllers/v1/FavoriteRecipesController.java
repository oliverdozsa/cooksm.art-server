package controllers.v1;

import com.typesafe.config.Config;
import dto.FavoriteRecipeCreateDto;
import dto.FavoriteRecipeDto;
import dto.PageDto;
import models.entities.FavoriteRecipe;
import models.repositories.FavoriteRecipeRepository;
import models.repositories.exceptions.BusinessLogicViolationException;
import models.repositories.exceptions.NotFoundException;
import models.repositories.Page;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
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

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class FavoriteRecipesController extends Controller {
    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private Config config;

    @Inject
    private FavoriteRecipeRepository repository;

    @Inject
    private FormFactory formFactory;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);

    private static final Logger.ALogger logger = Logger.of(RecipesController.class);

    public CompletionStage<Result> single(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return repository.single(id, jwt.getUserId())
                .thenApplyAsync(f -> {
                    if (f == null) {
                        return notFound();
                    }

                    FavoriteRecipeDto dto = DtoMapper.toDto(f);
                    return ok(Json.toJson(dto));
                }, httpExecutionContext.current())
                .exceptionally(mapException);
    }

    public CompletionStage<Result> allOfUser(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return repository.allOfUser(jwt.getUserId())
                .thenApplyAsync(FavoriteRecipesController::toResult, httpExecutionContext.current())
                .exceptionally(mapException);
    }

    public CompletionStage<Result> create(Http.Request request) {
        Form<FavoriteRecipeCreateDto> form = formFactory.form(FavoriteRecipeCreateDto.class)
                .bindFromRequest(request);

        if (form.hasErrors()) {
            return supplyAsync(() -> badRequest(form.errorsAsJson()));
        } else {
            FavoriteRecipeCreateDto fr = form.get();
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
            return repository.create(jwt.getUserId(), fr.getRecipeId())
                    .thenApplyAsync(p -> {
                                String location = routes.FavoriteRecipesController
                                        .single(p).absoluteURL(request);
                                return created().withHeader(LOCATION, location);
                            },
                            httpExecutionContext.current())
                    .exceptionally(mapException);
        }
    }

    public CompletionStage<Result> delete(Long id, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
        return repository.delete(id, jwt.getUserId())
                .thenApplyAsync(r -> {
                    if (r) {
                        return (Result) noContent();
                    } else {
                        return badRequest();
                    }
                }, httpExecutionContext.current())
                .exceptionally(mapException);
    }

    private static Result toResult(Page<FavoriteRecipe> page) {
        List<FavoriteRecipeDto> favoriteRecipes = page.getItems()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageDto<FavoriteRecipeDto> pageDto = new PageDto<>(favoriteRecipes, page.getTotalCount());

        return ok(Json.toJson(pageDto));
    }
}
