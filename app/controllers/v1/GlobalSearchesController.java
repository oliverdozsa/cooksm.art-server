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
    private GlobalSearchRepository repository;

    private static final Logger.ALogger logger = Logger.of(GlobalSearchesController.class);

    public CompletionStage<Result> all() {
        logger.info("all()");
        return repository.all()
                .thenApply(l -> {
                    List<GlobalSearchDto> dtoList = l.stream().map(DtoMapper::toDto)
                            .collect(Collectors.toList());
                    return ok(Json.toJson(dtoList));
                });
    }
}
