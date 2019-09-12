package controllers.v1;

import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class SourcePagesController extends Controller {
    public CompletionStage<Result> sourcePages(){
        return completedFuture(noContent());
    }
}
