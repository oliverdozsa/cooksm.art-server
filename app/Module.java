import com.google.inject.AbstractModule;
import models.repositories.FavoriteRecipeRepository;
import models.repositories.IngredientNameRepository;
import models.repositories.RecipeRepository;
import models.repositories.SourcePageRepository;
import models.repositories.imp.EbeanFavoriteRecipeRepository;
import models.repositories.imp.EbeanIngredientNameRepository;
import models.repositories.imp.EbeanRecipeRepository;
import models.repositories.imp.EbeanSourcePageRepository;
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
        bind(JwtValidator.class).to(JwtValidatorImp.class).asEagerSingleton();
    }
}
