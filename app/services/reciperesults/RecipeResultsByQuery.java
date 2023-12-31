package services.reciperesults;

import lombokized.dto.PageDto;
import lombokized.dto.RecipeDto;
import play.Logger;
import play.data.Form;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import services.RecipesService;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;
import static play.mvc.Results.*;

public abstract class RecipeResultsByQuery {
    private static final Logger.ALogger logger = Logger.of(RecipeResultsByQuery.class);

    protected RecipesService service;

    public RecipeResultsByQuery(RecipesService service) {
        this.service = service;
    }

    public abstract CompletionStage<Result> page(Form<RecipesQueryParams.Params> form, Http.Request request);

    protected Result toResult(PageDto<RecipeDto> pageDto) {
        return ok(toJson(pageDto));
    }

    protected <T> CompletionStage<Result> pageOrBadRequest(Form<T> form, Function<T, CompletionStage<Result>> resultProducer) {
        if (form.hasErrors()) {
            logger.warn("pageOrBadRequest(): form has error!");
            return completedFuture(badRequest(form.errorsAsJson()));
        } else {
            T formValue = form.get();
            logger.info("pageOrBadRequest(): formValue = {}", formValue);
            return resultProducer.apply(formValue);
        }
    }

    protected CompletionStage<Result> getResultFor(CompletionStage<PageDto<RecipeDto>> pageOfRecipesStage) {
        return pageOfRecipesStage.thenApplyAsync(this::toResult);
    }
}
