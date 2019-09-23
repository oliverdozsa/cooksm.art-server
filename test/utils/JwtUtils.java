package utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.AccessLevel;
import lombok.Builder;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class JwtUtils {
    @Builder(builderClassName = "Bldr", access = AccessLevel.PUBLIC)
    static String createToken(Long timeOffset, Long userId, String issuer, String secret) {
        Date date = new Date(new Date().getTime() + timeOffset);

        try {
            return JWT.create()
                    .withIssuer(issuer)
                    .withClaim("user_id", userId)
                    .withExpiresAt(date)
                    .sign(Algorithm.HMAC256(secret));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void addJwtTokenTo(Http.RequestBuilder httpRequest, String token) {
        httpRequest.header("Authorization", "Bearer " + token);
    }
}
