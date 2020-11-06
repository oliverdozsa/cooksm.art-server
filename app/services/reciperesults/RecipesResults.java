package services.reciperesults;

import play.Logger;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import security.SecurityUtils;
import security.VerifiedJwt;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

class RecipesResults {
    private static final Logger.ALogger logger = Logger.of(RecipesResults.class);

    public static Function<RecipesQueryParams.Params, CompletionStage<Result>> determine(Http.Request request, Producers producers) {
        if (SecurityUtils.hasVerifiedJwt(request)) {
            logger.info("determine(): recipe request is authenticated.");
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
            Long userId = jwt.getUserId();
            return p -> producers.auth.apply(p, userId);
        } else {
            logger.info("determine(): recipe request is unauthenticated.");
            return producers.unauth;
        }
    }

    public static class Producers {
        public Function<RecipesQueryParams.Params, CompletionStage<Result>> unauth;
        public BiFunction<RecipesQueryParams.Params, Long, CompletionStage<Result>> auth;
    }
}
