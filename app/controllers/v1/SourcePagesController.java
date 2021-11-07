package controllers.v1;

import data.DatabaseExecutionContext;
import data.entities.SourcePage;
import data.repositories.SourcePageRepository;
import lombokized.dto.PageDto;
import lombokized.dto.SourcePageDto;
import lombokized.repositories.Page;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import services.DtoMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.libs.Json.toJson;

public class SourcePagesController extends Controller {
    @Inject
    private SourcePageRepository repository;

    @Inject
    private DatabaseExecutionContext dbExecContext;

    private static final Logger.ALogger logger = Logger.of(SourcePagesController.class);

    public CompletionStage<Result> sourcePages() {
        logger.info("sourcePages()");
        return supplyAsync(() -> {
            Page<SourcePage> sourcePagesPage = repository.allSourcePages();
            return toResult(sourcePagesPage);
        }, dbExecContext);
    }

    private static Result toResult(Page<SourcePage> p) {
        List<SourcePageDto> l = p.getItems()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(l, p.getTotalCount())));
    }
}
