package services.reciperesults;

import play.data.Form;
import play.mvc.Http;
import play.mvc.Result;
import queryparams.RecipesQueryParams;
import security.SecurityUtils;
import security.VerifiedJwt;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

class RecipesResults {
    public static Function<RecipesQueryParams.Params, CompletionStage<Result>> determine(
            RecipesQueryParams.Params params, Http.Request request, Producers producers) {
        if (SecurityUtils.hasVerifiedJwt(request) && Boolean.TRUE.equals(params.useFavoritesOnly)) {
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
            Long userId = jwt.getUserId();
            return p -> producers.auth.apply(p, userId);
        } else {
            return producers.unauth;
        }
    }

    public static class Producers {
        public Function<RecipesQueryParams.Params, CompletionStage<Result>> unauth;
        public BiFunction<RecipesQueryParams.Params, Long, CompletionStage<Result>> auth;
    }
}
