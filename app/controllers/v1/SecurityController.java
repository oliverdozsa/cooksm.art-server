package controllers.v1;

import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;

public class SecurityController extends Controller {
    public static final String ISSUER = "receptnekem";

    public CompletionStage<Result> loginThroughGoogle(Http.Request request) {
        return null;
    }

    public CompletionStage<Result> loginThroughFacebook(Http.Request request) {
        return null;
    }
}
