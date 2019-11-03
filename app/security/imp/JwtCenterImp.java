package security.imp;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.Logger;
import play.libs.F.Either;
import security.JwtCenter;
import security.VerifiedJwt;

import javax.inject.Inject;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtCenterImp implements JwtCenter {
    private JWTVerifier verifier;
    private Config config;
    private Algorithm algorithm;
    private String issuer;

    private static Logger.ALogger logger = Logger.of(JwtCenter.class);

    @Inject
    public JwtCenterImp(Config config) {
        this.config = config;
        issuer = config.getString("receptnekem.jwt.issuer");

        String secret = config.getString("play.http.secret.key");
        algorithm = Algorithm.HMAC256(secret);

        verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    @Override
    public Either<Error, VerifiedJwt> verify(String token) {
        try{
            DecodedJWT jwt = verifier.verify(token);
            return Either.Right(new VerifiedJwtImp(jwt, config));
        } catch (JWTVerificationException e) {
            logger.warn("Failed to verify token!", e);
            return Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }

    @Override
    public String create(Long userId) {
        String claim = config.getString("receptnekem.jwt.useridclaim");
        int expiryMins = config.getInt("receptnekem.jwt.expiry.mins");
        return JWT.create()
                .withIssuer(issuer)
                .withClaim(claim, userId)
                .withExpiresAt(Date.from(Instant.now().plus(expiryMins, ChronoUnit.MINUTES)))
                .sign(algorithm);
    }
}
