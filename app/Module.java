import com.google.inject.AbstractModule;
import data.repositories.IngredientNameRepository;
import data.repositories.IngredientTagRepository;
import data.repositories.RecipeRepository;
import data.repositories.SourcePageRepository;
import data.repositories.imp.EbeanIngredientNameRepository;
import data.repositories.imp.EbeanIngredientTagRepository;
import data.repositories.imp.EbeanRecipeRepository;
import data.repositories.imp.EbeanSourcePageRepository;
import services.RecipesService;

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
        bind(RecipesService.class);
    }
}
