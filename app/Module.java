import com.google.inject.AbstractModule;
import models.repositories.IngredientNameRepository;
import models.repositories.RecipeRepository;
import models.repositories.imp.EbeanIngredientNameRepository;
import models.repositories.imp.EbeanRecipeRepository;

/**
 * Module for bindings.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(IngredientNameRepository.class).to(EbeanIngredientNameRepository.class).asEagerSingleton();
        bind(RecipeRepository.class).to(EbeanRecipeRepository.class).asEagerSingleton();
    }
}
