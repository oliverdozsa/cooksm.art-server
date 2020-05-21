package controllers.v1;

import com.typesafe.config.Config;
import lombokized.dto.PageDto;
import lombokized.dto.RecipeDto;
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
import services.RecipesService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;


public class RecipesController extends Controller {
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
        RecipesQueryParamsRetrieve retriever = new RecipesQueryParamsRetrieve(formFactory, request);
        Form<RecipesQueryParams.Params> form = retriever.retrieve();

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            return refineRequest(form);
        }
    }

    public CompletionStage<Result> singleRecipe(Long id, Long languageId) {
        return service.single(id, languageId)
                .thenApplyAsync(this::toResult, executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> refineRequest(Form<RecipesQueryParams.Params> form) {
        String searchModeStr = form.get().searchMode;
        RecipesQueryParams.SearchMode searchMode = RecipesQueryParams.Params.toEnum(searchModeStr);

        if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_NUMBER) {
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNumber);
        } else if (searchMode == RecipesQueryParams.SearchMode.COMPOSED_OF_RATIO) {
            return pageOrBadRequest(form, this::getRecipesForQueryTypeRatio);
        } else if (searchMode == RecipesQueryParams.SearchMode.NONE) {
            return pageOrBadRequest(form, this::getRecipesForQueryTypeNone);
        } else {
            logger.warn("refineRequestBy(): unknown search mode! searchMode = {}", searchMode);
            ValidationError ve = new ValidationError("", "Unkown search mode!");
            return completedFuture(badRequest(Json.toJson(ve.messages())));
        }
    }

    private CompletionStage<Result> getRecipesForQueryTypeNumber(RecipesQueryParams.Params params) {
        return service.pageOfQueryTypeNumber(params)
                .thenApplyAsync(this::toResult, executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeRatio(RecipesQueryParams.Params params) {
        return service.pageOfQueryTypeRatio(params)
                .thenApplyAsync(this::toResult, executionContext.current())
                .exceptionally(mapException);
    }

    private CompletionStage<Result> getRecipesForQueryTypeNone(RecipesQueryParams.Params params) {
        return service.pageOfQueryTypeNone(params)
                .thenApplyAsync(this::toResult)
                .exceptionally(mapException);
    }

    private Result toResult(PageDto<RecipeDto> pageDto) {
        return ok(toJson(pageDto));
    }

    private Result toResult(RecipeDto dto) {
        if (dto == null) {
            return notFound();
        } else {
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
}
