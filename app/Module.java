import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import models.repositories.*;
import models.repositories.imp.*;
import security.JwtCenter;
import security.SocialTokenVerifier;
import security.imp.JwtCenterImp;
import security.imp.SocialTokenVerifierFacebookImp;
import security.imp.SocialTokenVerifierGoogleImp;

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
    }
}
