package security.imp;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.typesafe.config.Config;
import play.libs.F.Either;
import security.JwtValidator;
import security.VerifiedJwt;

import javax.inject.Inject;

public class JwtValidatorImp implements JwtValidator {
    private JWTVerifier verifier;
    private Config config;

    @Inject
    public JwtValidatorImp(Config config) throws Exception {
        this.config = config;
        String secret = config.getString("receptnekem.jwt.useridclaim");
        String issuer = config.getString("receptnekem.jwt.issuer");;

        verifier = JWT.require(Algorithm.HMAC256(secret))
                .withIssuer(issuer)
                .build();
    }

    @Override
    public Either<Error, VerifiedJwt> verify(String token) {
        try{
            DecodedJWT jwt = verifier.verify(token);
            return Either.Right(new VerifiedJwtImp(jwt, config));
        } catch (JWTVerificationException e) {
            return Either.Left(Error.ERR_INVALID_SIGNATURE_OR_CLAIM);
        }
    }
}
