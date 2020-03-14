import com.google.inject.AbstractModule;
import models.repositories.IngredientNameRepository;
import models.repositories.IngredientTagRepository;
import models.repositories.RecipeRepository;
import models.repositories.SourcePageRepository;
import models.repositories.imp.EbeanIngredientNameRepository;
import models.repositories.imp.EbeanIngredientTagRepository;
import models.repositories.imp.EbeanRecipeRepository;
import models.repositories.imp.EbeanSourcePageRepository;

/**
 * Module for bindings.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(IngredientNameRepository.class).to(EbeanIngredientNameRepository.class).asEagerSingleton();
        bind(RecipeRepository.class).to(EbeanRecipeRepository.class).asEagerSingleton();
        bind(SourcePageRepository.class).to(EbeanSourcePageRepository.class).asEagerSingleton();
        bind(IngredientTagRepository.class).to(EbeanIngredientTagRepository.class).asEagerSingleton();
    }
}
