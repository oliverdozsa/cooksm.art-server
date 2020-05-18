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
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;
import static controllers.v1.RecipeControllerQueryMapping.*;


public class RecipesController extends Controller {
    @Inject
    private RecipeRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext executionContext;

    @Inject
    private Config config;

    private Function<Throwable, Result> mapException = t -> {
        logger.error("Internal Error!", t.getCause());

        if (t.getCause() instanceof IllegalArgumentException) {
            return badRequest(Json.toJson(new ValidationError("", t.getMessage()).messages()));
        }

        return internalServerError();
    };

    private static final Logger.ALogger logger = Logger.of(RecipesController.class);

    public CompletionStage<Result> pageRecipes(Http.Request request) {
        Either<JsonNode, RecipesControllerQuery.SearchMode> jsonNodeOrSearchMode =
                retreiveSearchMode(request);

        if (jsonNodeOrSearchMode.isLeft()) {
            // Request has error
            logger.warn("pageRecipes(): request has error!");
            JsonNode errorJson = jsonNodeOrSearchMode.left().get();
            return completedFuture(badRequest(errorJson));
        } else {
            RecipesControllerQuery.SearchMode searchMode = jsonNodeOrSearchMode.right().get();
            logger.info("pageRecipes(): searchMode = {}", searchMode);
            return refineRequestBy(searchMode, request);
        }
    }

    public CompletionStage<Result> singleRecipe(Long id, Long languageId) {
        return repository.single(id).thenApply(r -> toResult(r, languageId))
                .exceptionally(mapException);
    }

    private CompletionStage<Result> refineRequestBy(RecipesControllerQuery.SearchMode searchMode, Http.Request request) {
        if (searchMode == RecipesControllerQuery.SearchMode.COMPOSED_OF_NUMBER) {
            Form<RecipesControllerQuery.Params> form = formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOf.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNumber);
        } else if (searchMode == RecipesControllerQuery.SearchMode.COMPOSED_OF_RATIO) {
            Form<RecipesControllerQuery.Params> form = formFactory.form(RecipesControllerQuery.Params.class, RecipesControllerQuery.VGRecSearchModeComposedOfRatio.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeRatio);
        } else if (searchMode == RecipesControllerQuery.SearchMode.NONE) {
            Form<RecipesControllerQuery.Params> form = formFactory.form(RecipesControllerQuery.Params.class)
                    .bindFromRequest(request);
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNone);
        } else {
            logger.warn("refineRequestBy(): unknown search mode! searchMode = {}", searchMode);
            ValidationError ve = new ValidationError("", "Unkown search mode!");
            return completedFuture(badRequest(Json.toJson(ve.messages())));
        }


    }

    private CompletionStage<Result> getRecipesForQueryTypeNumber(RecipesControllerQuery.Params params) {
        return repository.pageOfQueryTypeNumber(toQueryTypeNumber(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeRatio(RecipesControllerQuery.Params params) {
        return repository.pageOfQueryTypeRatio(toQueryTypeRatio(params))
                .thenApplyAsync(page -> toResult(page, params.languageId), executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNone(RecipesControllerQuery.Params params) {
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

    private Either<JsonNode, RecipesControllerQuery.SearchMode> retreiveSearchMode(Http.Request request) {
        // Get form without groups to access search mode.
        Form<RecipesControllerQuery.Params> form =
                formFactory.form(RecipesControllerQuery.Params.class)
                        .bindFromRequest(request);

        if (form.hasErrors()) {
            return new Left<>(form.errorsAsJson());
        } else {
            String searchModeStr = form.get().searchMode;
            RecipesControllerQuery.SearchMode searchMode = RecipesControllerQuery.Params.toEnum(searchModeStr);

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
