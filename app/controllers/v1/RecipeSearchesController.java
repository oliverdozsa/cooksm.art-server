package controllers.v1;

import com.typesafe.config.Config;
import dto.PageDto;
import dto.RecipeSearchDto;
import models.entities.RecipeSearch;
import models.repositories.RecipeSearchRepository;
import play.Logger;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import security.SecurityUtils;
import security.VerifiedJwt;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RecipeSearchesController extends Controller {
    @Inject
    private RecipeSearchRepository repository;

    @Inject
    private FormFactory formFactory;

    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private Config config;

    private Function<Throwable, Result> mapException = new DefaultExceptionMapper(logger);

    private static final Logger.ALogger logger = Logger.of(RecipeSearchesController.class);

    public CompletionStage<Result> globals() {
        return repository.globals().thenApplyAsync(p -> {
            List<RecipeSearchDto> searchDtos = p.getItems().stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());

            return ok(Json.toJson(new PageDto<>(searchDtos, p.getTotalCount())));
        }, httpExecutionContext.current());
    }

    public CompletionStage<Result> userSearches(Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return repository.userSearches(jwt.getUserId())
                .thenApplyAsync(p -> {
                    List<RecipeSearchDto> searchDtos = p.getItems().stream()
                            .map(DtoMapper::toDto)
                            .collect(Collectors.toList());

                    return ok(Json.toJson(new PageDto<>(searchDtos, p.getTotalCount())));
                }, httpExecutionContext.current())
                .exceptionally(mapException);
    }

    public CompletionStage<Result> userSearch(Long entityId, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return repository.userSearch(jwt.getUserId(), entityId)
                .thenApplyAsync(e -> {
                    RecipeSearchDto dto = DtoMapper.toDto(e);
                    return ok(Json.toJson(e));
                }, httpExecutionContext.current())
                .exceptionally(mapException);
    }
}
