package controllers.v1;

import dto.PageDto;
import dto.SourcePageDto;
import models.entities.SourcePage;
import models.repositories.Page;
import models.repositories.SourcePageRepository;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static play.libs.Json.toJson;

public class SourcePagesController extends Controller {
    @Inject
    private HttpExecutionContext httpExecutionContext;

    @Inject
    private SourcePageRepository repository;

    public CompletionStage<Result> sourcePages(){
        return repository.allSourcePages()
                .thenApplyAsync(SourcePagesController::toResult, httpExecutionContext.current());
    }

    private static Result toResult(Page<SourcePage> p){
        List<SourcePageDto> l = p.getItems()
                .stream()
                .map(DtoMapper::toDto)
                .collect(Collectors.toList());

        return ok(toJson(new PageDto<>(l, p.getTotalCount())));
    }
}
