package controllers.v1;

import play.Logger;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

public class UserSearchesController extends Controller {
    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());

    private static final Logger.ALogger logger = Logger.of(UserSearchesController.class);

    public CompletionStage<Result> create(Http.Request request) {
        // TODO: Input: name, and query
        return null;
    }

    public CompletionStage<Result> single(Long id) {
        // TODO: Should return name and query content
        return null;
    }

    public CompletionStage<Result> update(Long id, Http.Request request) {
        // TODO: Input: name, and query
        return null;
    }

    public CompletionStage<Result> all() {
        // TODO: Should return ids, and names of the queries. All data should be returned
        return null;
    }

    public CompletionStage<Result> delete(Long id) {
        // TODO
        return null;
    }
}
