package security.filter;

import play.libs.typedmap.TypedKey;
import security.VerifiedJwt;

public class Attrs {
    public static final TypedKey<VerifiedJwt> VERIFIED_JWT = TypedKey.create("verifiedJwt");
}
