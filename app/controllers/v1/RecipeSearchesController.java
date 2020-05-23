package controllers.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import lombokized.dto.RecipeSearchDto;
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
import services.RecipeSearchService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.completedFuture;

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
        if (id == null || id.length() == 0) {
            logger.warn("ID is empty!");
            ValidationError error = new ValidationError("", "ID is empty");
            return completedFuture(badRequest(Json.toJson(error.messages())));
        }

        return service.get(id)
                .thenApplyAsync(this::toResult)
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> create(Http.Request request) {
        RecipesQueryParamsRetrieve retriever = new RecipesQueryParamsRetrieve(formFactory, request);
        Form<RecipesQueryParams.Params> form = retriever.retrieve();

        if (form.hasErrors()) {
            JsonNode errorsJson = form.errorsAsJson();
            logger.warn("Query params has errors! error = {}", errorsJson.toPrettyString());
            return completedFuture(badRequest(errorsJson));
        }

        return service.create(form.get(), false)
                .thenApplyAsync(id -> {
                    String location = routes.RecipeSearchesController.get(id).absoluteURL(request);
                    return created().withHeader(LOCATION, location);
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    private Result toResult(RecipeSearchDto dto) {
        if (dto == null) {
            return notFound();
        }

        return ok(Json.toJson(dto));
    }
}
