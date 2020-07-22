package security;

import play.mvc.Http;
import security.filter.Attrs;

public class SecurityUtils {
    // Assumes that JwtFilter added the verified JWT as attribute to the request.
    public static VerifiedJwt getFromRequest(Http.Request httpRequest) {
        return httpRequest.attrs().get(Attrs.VERIFIED_JWT);
    }

    public static boolean hasVerifiedJwt(Http.Request request) {
        return request.attrs().containsKey(Attrs.VERIFIED_JWT);
    }
}