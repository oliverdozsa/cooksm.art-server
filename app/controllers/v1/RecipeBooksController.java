package controllers.v1;

import play.Logger;
import play.data.validation.ValidationError;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class RecipeBooksController extends Controller {
    private Function<Throwable, Result> mapException = t -> {
        logger.error("Internal Error!", t.getCause());

        if (t.getCause() instanceof IllegalArgumentException) {
            return badRequest(Json.toJson(new ValidationError("", t.getMessage()).messages()));
        }

        return internalServerError();
    };

    private static final Logger.ALogger logger = Logger.of(RecipeBooksController.class);

    public CompletionStage<Result> create(Http.Request request) {
        // TODO
        return null;
    }

    public CompletionStage<Result> all(Http.Request request) {
        // TODO
        return null;
    }
}
