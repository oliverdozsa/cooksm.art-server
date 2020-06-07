package controllers.v1;

import data.entities.GlobalSearch;
import data.entities.RecipeSearch;
import data.repositories.GlobalSearchRepository;
import lombokized.dto.GlobalSearchDto;
import lombokized.dto.PageDto;
import lombokized.repositories.Page;
import play.Logger;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.DtoMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class GlobalSearchesController extends Controller {
    @Inject
    private HttpExecutionContext executionContext;

    @Inject
    private GlobalSearchRepository repository;

    private static final Logger.ALogger logger = Logger.of(GlobalSearchesController.class);

    public CompletionStage<Result> all() {
        return repository.all()
                .thenApply(this::toResult);
    }

    private Result toResult(Page<GlobalSearch> page){
        List<GlobalSearchDto> dtos = page.getItems().stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        PageDto<GlobalSearchDto> pageDto = new PageDto<>(dtos, page.getTotalCount());
        return ok(Json.toJson(pageDto));
    }
}
