package security.imp;

import data.entities.User;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import lombokized.security.VerifiedFacebookUserInfo;
import lombokized.security.VerifiedUserInfo;
import play.Environment;
import play.Logger;
import play.db.ebean.EbeanConfig;
import security.SocialTokenVerifier;
import security.TokenVerificationException;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SocialTokenVerifierDevModeImp implements SocialTokenVerifier {
    private EbeanServer ebean;
    private Environment environment;

    private static Logger.ALogger logger = Logger.of(SocialTokenVerifierDevModeImp.class);

    @Inject
    public SocialTokenVerifierDevModeImp(Environment environment, EbeanConfig dbConfig) {
        this.environment = environment;
        ebean = Ebean.getServer(dbConfig.defaultServer());
        deleteDevUserIfPresent();
    }

    @Override
    public CompletionStage<VerifiedUserInfo> verify(String token) {
        return supplyAsync(() -> {
            if (environment.isProd()) {
                throw new DevVerifierException("Dev verifier should not be used in production!");
            }

            return createDevUserInfo();
        });
    }

    private VerifiedUserInfo createDevUserInfo() {
        return new VerifiedFacebookUserInfo("Recept Nekem", "dev@receptnekem.hu", "123456");
    }

    private static class DevVerifierException extends TokenVerificationException {
        public DevVerifierException(String message) {
            super(message);
        }
    }

    private void deleteDevUserIfPresent() {
        User user = ebean.createQuery(User.class)
                .where()
                .eq("email", "dev@receptnekem.hu")
                .findOne();

        if (user != null) {
            logger.info("deleteDevUserIfPresent(): dev user present, deleting.");
            ebean.delete(user);
        } else {
            logger.info("deleteDevUserIfPresent(): dev user not present.");
        }
    }
}
