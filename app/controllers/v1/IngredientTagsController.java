package controllers.v1;

import data.repositories.IngredientTagRepository;
import lombokized.dto.IngredientTagDto;
import lombokized.queryparams.IngredientTagQueryParams;
import lombokized.repositories.Page;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;
import services.IngredientTagsService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static play.libs.Json.toJson;

public class IngredientTagsController extends Controller {
    @Inject
    private IngredientTagRepository repository;

    @Inject
    private IngredientTagsService service;

    @Inject
    private FormFactory formFactory;

    private static final Logger.ALogger logger = Logger.of(IngredientTagsController.class);

    public CompletionStage<Result> page(Http.Request request) {
        Form<IngredientTagQueryParams> form =
                formFactory.form(IngredientTagQueryParams.class).bindFromRequest(request);

        if (form.hasErrors()) {
            logger.warn("page(): form has errors!");
            return completedFuture(badRequest(form.errorsAsJson()));
        }

        IngredientTagQueryParams queryParams = form.get();
        queryParams.setLimit(queryParams.getLimit() == null ? 25 : queryParams.getLimit());
        queryParams.setOffset(queryParams.getOffset() == null ? 0 : queryParams.getOffset());
        logger.info("page(): queryParams = {}", queryParams);

        if(SecurityUtils.hasVerifiedJwt(request)) {
            logger.info("page(): request is authenticated.");
            VerifiedJwt jwt = SecurityUtils.getFromRequest(request);
            return service.page(queryParams, jwt.getUserId())
                    .thenApplyAsync(this::toResult);

        } else {
            logger.info("page(): request is unauthenticated.");
            return service.page(queryParams)
                    .thenApplyAsync(this::toResult);
        }
    }

    private Result toResult(Page<IngredientTagDto> pageDto) {
        return ok(toJson(pageDto));
    }
}
