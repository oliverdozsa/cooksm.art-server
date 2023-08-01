package security.imp;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import security.VerifiedJwt;

public class VerifiedJwtImp implements VerifiedJwt {
    private final Long userId;

    public VerifiedJwtImp(DecodedJWT jwt, Config config) {
        String userIdClaim = config.getString("cooksm.art.jwt.useridclaim");
        userId = jwt.getClaim(userIdClaim).asLong();
    }

    @Override
    public Long getUserId() {
        return userId;
    }
}
