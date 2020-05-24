import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import data.repositories.*;
import data.repositories.imp.*;
import security.JwtCenter;
import security.SocialTokenVerifier;
import security.imp.JwtCenterImp;
import security.imp.SocialTokenVerifierFacebookImp;
import security.imp.SocialTokenVerifierGoogleImp;
import services.LanguageService;
import services.RecipeSearchService;
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
        bind(FavoriteRecipeRepository.class).to(EbeanFavoriteRecipeRepository.class).asEagerSingleton();
        bind(RecipeSearchRepository.class).to(EbeanRecipeSearchRepository.class).asEagerSingleton();
        bind(JwtCenter.class).to(JwtCenterImp.class).asEagerSingleton();
        bind(UserRepository.class).to(EbeanUserRepository.class).asEagerSingleton();
        bind(SocialTokenVerifier.class).annotatedWith(Names.named("Google")).to(SocialTokenVerifierGoogleImp.class).asEagerSingleton();
        bind(SocialTokenVerifier.class).annotatedWith(Names.named("Facebook")).to(SocialTokenVerifierFacebookImp.class).asEagerSingleton();
        bind(IngredientTagRepository.class).to(EbeanIngredientTagRepository.class).asEagerSingleton();
        bind(RecipesService.class);
        bind(RecipeSearchRepository.class).to(EbeanRecipeSearchRepository.class).asEagerSingleton();
        bind(UserSearchRepository.class).to(EbeanUserSearchRepository.class).asEagerSingleton();
        bind(GlobalSearchRepository.class).to(EbeanGlobalSearchRepository.class).asEagerSingleton();
        bind(RecipeSearchService.class);
        bind(LanguageService.class);
    }
}
