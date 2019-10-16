package controllers.v1;

import com.typesafe.config.Config;
import dto.PageDto;
import dto.RecipeSearchCreateUpdateDto;
import dto.RecipeSearchDto;
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

import static java.util.concurrent.CompletableFuture.completedFuture;

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
    private Function<Throwable, Result> mapExceptionWithUnpack = e -> mapException.apply(e.getCause());


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
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> userSearch(Long entityId, Http.Request request) {
        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return repository.userSearch(jwt.getUserId(), entityId)
                .thenApplyAsync(e -> {
                    RecipeSearchDto dto = DtoMapper.toDto(e);
                    return ok(Json.toJson(dto));
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }

    public CompletionStage<Result> create(Http.Request request) {
        RecipeSearchCreateUpdateDto dto;
        try {
            dto = new RecipeSearchCreatorUpdater(formFactory, request).create();
        } catch (Exception e) {
            return completedFuture(mapException.apply(e));
        }

        VerifiedJwt jwt = SecurityUtils.getFromRequest(request);

        return repository.create(jwt.getUserId(), dto.getName(), dto.getQuery())
                .thenApplyAsync(id -> {
                    String location = routes.RecipeSearchesController.userSearch(id).absoluteURL(request);
                    return created().withHeader(LOCATION, location);
                }, httpExecutionContext.current())
                .exceptionally(mapExceptionWithUnpack);
    }
}
