import com.google.inject.AbstractModule;
import models.repositories.*;
import models.repositories.imp.*;
import security.JwtValidator;
import security.imp.JwtValidatorImp;

/**
 * Module for bindings.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(IngredientNameRepository.class).to(EbeanIngredientNameRepository.class).asEagerSingleton();
        bind(RecipeRepository.class).to(EbeanRecipeRepository.class).asEagerSingleton();
        bind(SourcePageRepository.class).to(EbeanSourcePageRepository.class).asEagerSingleton();
        bind(FavoriteRecipeRepository.class).to(EbeanFavoriteRecipeRepository.class).asEagerSingleton();
        bind(RecipeSearchRepository.class).to(EbeanRecipeSearchRepository.class).asEagerSingleton();
        bind(JwtValidator.class).to(JwtValidatorImp.class).asEagerSingleton();
    }
}
