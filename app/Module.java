import com.google.inject.AbstractModule;
import models.repositories.IngredientNameRepository;
import models.repositories.imp.JPAIngredientNameRepository;

/**
 * Module for bindings.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(IngredientNameRepository.class).to(JPAIngredientNameRepository.class).asEagerSingleton();
    }
}
