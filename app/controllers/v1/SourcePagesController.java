package controllers.v1;

import lombokized.dto.PageDto;
import lombokized.dto.SourcePageDto;
import data.entities.SourcePage;
import lombokized.repositories.Page;
import data.repositories.SourcePageRepository;
import play.Logger;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;
import services.DtoMapper;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static play.libs.Json.toJson;

public class SourcePagesController extends Controller {
    @Inject
    private SourcePageRepository repository;

    private static final Logger.ALogger logger = Logger.of(SourcePagesController.class);

    public CompletionStage<Result> sourcePages() {
        logger.info("sourcePages()");
        return repository.allSourcePages()
                .thenApplyAsync(SourcePagesController::toResult);
    }

    private static Result toResult(Page<SourcePage> p) {
        List<SourcePageDto> l = p.getItems()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(l, p.getTotalCount())));
    }
}
