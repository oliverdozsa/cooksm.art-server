package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.typesafe.config.Config;
import play.mvc.Http;

import java.util.Date;

public class JwtTestUtils {
    private static final Long DEFAULT_TIME_OFFSET = 5000L;

    public static String createToken(Long timeOffset, Long userId, Config config) {
        Date date = new Date(new Date().getTime() + timeOffset);

        return JWT.create()
                .withIssuer(config.getString("cooksm.art.jwt.issuer"))
                .withClaim(config.getString("cooksm.art.jwt.useridclaim"), userId)
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(config.getString("play.http.secret.key")));
    }

    public static String createToken(Long userId, Config config) {
        return createToken(DEFAULT_TIME_OFFSET, userId, config);
    }

    public static void addJwtTokenTo(Http.RequestBuilder httpRequest, String token) {
        httpRequest.header("Authorization", "Bearer " + token);
    }
}
