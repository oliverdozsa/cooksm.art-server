package controllers.v1;

import com.typesafe.config.Config;
import models.repositories.FavoriteRecipeRepository;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

public class FavoriteRecipesController extends Controller {
    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private Config config;

    @Inject
    private FavoriteRecipeRepository repository;

    public CompletionStage<Result> single(Long id) {
        // TODO
        return repository.single(id)
                .thenApplyAsync(f -> new Result(NOT_IMPLEMENTED), httpExecutionContext.current());
    }

    public CompletionStage<Result> allOfUser(Long userId) {
        // TODO
        return repository.allOfUser(userId)
                .thenApplyAsync(p -> new Result(NOT_IMPLEMENTED), httpExecutionContext.current());
    }

    public CompletionStage<Result> create() {
        // TODO
        return repository.create(null, null)
                .thenApplyAsync(p -> new Result(NOT_IMPLEMENTED), httpExecutionContext.current());
    }

}
