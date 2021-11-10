package controllers.v1;

import data.DatabaseExecutionContext;
import data.entities.GlobalSearch;
import data.repositories.GlobalSearchRepository;
import lombokized.dto.GlobalSearchDto;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.DtoMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class GlobalSearchesController extends Controller {
    @Inject
    private GlobalSearchRepository repository;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(GlobalSearchesController.class);

    public CompletionStage<Result> all() {
        logger.info("all()");
        return supplyAsync(() -> {
            List<GlobalSearch> globalSearches = repository.all();
            List<GlobalSearchDto> dtoList = globalSearches.stream().map(DtoMapper::toDto)
                    .collect(Collectors.toList());
            return ok(Json.toJson(dtoList));
        }, dbExecContext);
    }
}
