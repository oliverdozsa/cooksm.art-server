import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import data.repositories.*;
import data.repositories.imp.*;
import io.ebean.EbeanServer;
import security.JwtCenter;
import security.SocialTokenVerifier;
import security.imp.JwtCenterImp;
import security.imp.SocialTokenVerifierDevModeImp;
import security.imp.SocialTokenVerifierFacebookImp;
import security.imp.SocialTokenVerifierGoogleImp;
import services.*;

/**
 * Module for bindings.
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(EbeanServer.class).toProvider(EbeanServerProvider.class);
        bind(IngredientNameRepository.class).to(EbeanIngredientNameRepository.class).asEagerSingleton();
        bind(RecipeRepository.class).to(EbeanRecipeRepository.class).asEagerSingleton();
        bind(SourcePageRepository.class).to(EbeanSourcePageRepository.class).asEagerSingleton();
        bind(FavoriteRecipeRepository.class).to(EbeanFavoriteRecipeRepository.class).asEagerSingleton();
        bind(RecipeSearchRepository.class).to(EbeanRecipeSearchRepository.class).asEagerSingleton();
        bind(JwtCenter.class).to(JwtCenterImp.class).asEagerSingleton();
        bind(UserRepository.class).to(EbeanUserRepository.class).asEagerSingleton();
        bind(SocialTokenVerifier.class).annotatedWith(Names.named("Google")).to(SocialTokenVerifierGoogleImp.class).asEagerSingleton();
        bind(SocialTokenVerifier.class).annotatedWith(Names.named("Facebook")).to(SocialTokenVerifierFacebookImp.class).asEagerSingleton();
        bind(SocialTokenVerifier.class).annotatedWith(Names.named("Dev")).to(SocialTokenVerifierDevModeImp.class).asEagerSingleton();
        bind(IngredientTagRepository.class).to(EbeanIngredientTagRepository.class).asEagerSingleton();
        bind(RecipesService.class).asEagerSingleton();
        bind(RecipeSearchRepository.class).to(EbeanRecipeSearchRepository.class).asEagerSingleton();
        bind(UserSearchRepository.class).to(EbeanUserSearchRepository.class).asEagerSingleton();
        bind(GlobalSearchRepository.class).to(EbeanGlobalSearchRepository.class).asEagerSingleton();
        bind(RecipeBookRepository.class).to(EbeanRecipeBookRepository.class).asEagerSingleton();
        bind(RecipeSearchService.class).asEagerSingleton();
        bind(LanguageService.class).asEagerSingleton();
        bind(UserSearchService.class).asEagerSingleton();
        bind(RecipeRepositoryQueryCheck.class).asEagerSingleton();
        bind(IngredientTagsService.class).asEagerSingleton();
        bind(RecipeBooksService.class).asEagerSingleton();
        bind(ShoppingListRepository.class).to(EbeanShoppingListRepository.class).asEagerSingleton();
        bind(ShoppingListService.class).asEagerSingleton();
        bind(MenuService.class).asEagerSingleton();
        bind(MenuRepository.class).to(EbeanMenuRepository.class).asEagerSingleton();
    }
}
