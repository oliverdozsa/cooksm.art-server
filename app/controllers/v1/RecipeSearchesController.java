package controllers.v1;

import com.typesafe.config.Config;
import data.repositories.RecipeSearchRepository;
import dto.RecipeSearchDto;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import services.RecipeSearchService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class RecipeSearchesController extends Controller {
    @Inject
    private RecipeSearchService service;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private Config config;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());


    private static final Logger.ALogger logger = Logger.of(RecipeSearchesController.class);

    public CompletionStage<Result> get(String id) {
        // TODO
        return completedFuture(notFound());
    }

    public CompletionStage<Result> create(Http.Request request) {
        RecipesQueryParamsRetrieve retriever = new RecipesQueryParamsRetrieve(formFactory, request);
        Form<RecipesQueryParams.Params> form = retriever.retrieve();

        if (form.hasErrors()) {
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        return service.create(form.get(), false)
                .thenApplyAsync(dto -> ok(Json.toJson(dto)), httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }
}
