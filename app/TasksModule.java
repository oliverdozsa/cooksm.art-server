import com.google.inject.AbstractModule;
import tasks.CleanExpiredRecipeSearchesTask;

public class TasksModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CleanExpiredRecipeSearchesTask.class).asEagerSingleton();
    }
}
