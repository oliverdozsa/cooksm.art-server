package security;

import play.libs.F.Either;

public interface JwtValidator {
    enum Error {
        ERR_INVALID_SIGNATURE_OR_CLAIM
    }

    Either<Error, VerifiedJwt> verify(String token);
}
