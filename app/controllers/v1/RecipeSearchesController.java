package controllers.v1;

import com.typesafe.config.Config;
import dto.PageDto;
import dto.RecipeSearchDto;
import models.repositories.RecipeSearchRepository;
import play.data.FormFactory;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
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

    public CompletionStage<Result> globals() {
        return repository.globals().thenApplyAsync(p -> {
            List<RecipeSearchDto> searchDtos = p.getItems().stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());

            return ok(Json.toJson(new PageDto<>(searchDtos, p.getTotalCount())));
        }, httpExecutionContext.current());
    }
}
