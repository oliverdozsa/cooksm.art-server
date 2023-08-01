package tasks;

import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Duration;
import services.RecipeSearchService;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class CleanExpiredRecipeSearchesTask {
    private final ActorSystem actorSystem;
    private final ExecutionContext executionContext;
    private int intervalSecs;
    private RecipeSearchService service;

    @Inject
    public CleanExpiredRecipeSearchesTask(ActorSystem actorSystem, ExecutionContext executionContext, Config config, RecipeSearchService service) {
        this.actorSystem = actorSystem;
        this.executionContext = executionContext;
        intervalSecs = config.getInt("cooksm.art.recipesearches.clean.interval.secs");
        this.service = service;
        this.initialize();
    }

    private void initialize() {
        this.actorSystem
                .scheduler()
                .scheduleAtFixedRate(
                        Duration.create(10, TimeUnit.SECONDS), // initialDelay
                        Duration.create(intervalSecs, TimeUnit.SECONDS), // interval
                        () -> service.deleteExpired(),
                        this.executionContext);
    }
}
