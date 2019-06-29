import com.google.inject.AbstractModule;
import models.repositories.IngredientNameRepository;
import models.repositories.imp.JPAIngredientNameRepository;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(IngredientNameRepository.class).to(JPAIngredientNameRepository.class).asEagerSingleton();
    }
}
