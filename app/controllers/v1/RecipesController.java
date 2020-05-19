package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import lombokized.dto.PageDto;
import lombokized.dto.RecipeDto;
import data.entities.Recipe;
import lombokized.repositories.Page;
import data.repositories.RecipeRepository;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;
import services.DtoMapper;
import services.RecipesService;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;
import static services.RecipesQueryParamsMapping.*;


public class RecipesController extends Controller {
    @Inject
    private RecipeRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext executionContext;

    @Inject
    private Config config;

    @Inject
    private RecipesService service;

    private Function<Throwable, Result> mapException = t -> {
        logger.error("Internal Error!", t.getCause());

        if (t.getCause() instanceof IllegalArgumentException) {
            return badRequest(Json.toJson(new ValidationError("", t.getMessage()).messages()));
        }

        return internalServerError();
    };

    private static final Logger.ALogger logger = Logger.of(RecipesController.class);

    public CompletionStage<Result> pageRecipes(Http.Request request) {
        Either<JsonNode, RecipesQueryParams.SearchMode> jsonNodeOrSearchMode =
                retreiveSearchMode(request);

        if (jsonNodeOrSearchMode.isLeft()) {
            // Request has error
            logger.warn("pageRecipes(): request has error!");
            JsonNode errorJson = jsonNodeOrSearchMode.left().get();
            return completedFuture(badRequest(errorJson));
        } else {
            RecipesQueryParams.SearchMode searchMode = jsonNodeOrSearchMode.right().get();
            logger.info("pageRecipes(): searchMode = {}", searchMode);
            return refineRequestBy(searchMode, request);
        }
    }

    public CompletionStage<Result> singleRecipe(Long id, Long languageId) {
        return repository.single(id).thenApply(r -> toResult(r, languageId))
                .exceptionally(mapException);
    }

    private CompletionStage<Result> refineRequestBy(RecipesQueryParams.SearchMode searchMode, Http.Request request) {
        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class, RecipesQueryParams.VGRecSearchModeComposedOf.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNumber);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class, RecipesQueryParams.VGRecSearchModeComposedOfRatio.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeRatio);
        } else if (searchMode == RecipesQueryParams.SearchMode.NONE) {
            Form<RecipesQueryParams.Params> form = formFactory.form(RecipesQueryParams.Params.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNone);
        } else {
            logger.warn("refineRequestBy(): unknown search mode! searchMode = {}", searchMode);
            ValidationError ve = new ValidationError("", "Unkown search mode!");
            return completedFuture(badRequest(Json.toJson(ve.messages())));
        }


    }

    private CompletionStage<Result> getRecipesForQueryTypeNumber(RecipesQueryParams.Params params) {
        return repository.pageOfQueryTypeNumber(toQueryTypeNumber(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeRatio(RecipesQueryParams.Params params) {
        return repository.pageOfQueryTypeRatio(toQueryTypeRatio(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNone(RecipesQueryParams.Params params) {
        return repository.pageOfQueryTypeNone(toCommon(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), executionContext.current())
                .exceptionally(mapException);
    }

    private Result toResult(Page<Recipe> page, Long languageId) {
        Long usedLanguageId = getLanguageIdOrDefault(languageId);
        List<RecipeDto> dtos = page.getItems()
                .stream()
                .map(entity -> DtoMapper.toDto(entity, usedLanguageId))
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(dtos, page.getTotalCount())));
    }

    private Result toResult(Recipe recipe, Long languageId) {
        if (recipe == null) {
            return notFound();

        } else {
            Long usedLanguageId = getLanguageIdOrDefault(languageId);
            RecipeDto dto = DtoMapper.toDto(recipe, usedLanguageId);
            return ok(toJson(dto));
        }
    }

    private <T> CompletionStage<Result> pageOrBadRequest(Form<T> form, Function<T, CompletionStage<Result>> resultProducer) {
        if (form.hasErrors()) {
            logger.warn("pageOrBadRequest(): form has error!");
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            T formValue = form.get();
            logger.info("pageOrBadRequest(): formValue = {}", formValue);
            return resultProducer.apply(formValue);
        }
    }

    private Long getDefaultLanguageId() {
        return config.getLong("openrecipes.default.languageid");
    }

    private Either<JsonNode, RecipesQueryParams.SearchMode> retreiveSearchMode(Http.Request request) {
        // Get form without groups to access search mode.
        Form<RecipesQueryParams.Params> form =
                formFactory.form(RecipesQueryParams.Params.class)
                        .bindFromRequest(request);

        if (form.hasErrors()) {
            return new Left<>(form.errorsAsJson());
        } else {
            String searchModeStr = form.get().searchMode;
            RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(searchModeStr);

            return new Right<>(searchMode);
        }
    }

    private Long getLanguageIdOrDefault(Long id) {
        if (id == null) {
            return getDefaultLanguageId();
        }

        if (id == 0L) {
            return getDefaultLanguageId();
        }

        return id;
    }
}
