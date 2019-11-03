/*
 * MIT License
 *
 * Copyright (c) 2017 Franz Granlund
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package security.filter;

import akka.stream.Materializer;
import com.typesafe.config.Config;
import security.JwtCenter;
import security.VerifiedJwt;
import play.Logger;
import play.libs.F.Either;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.HandlerDef;
import play.routing.Router;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import static play.mvc.Results.forbidden;

public class JwtFilter extends Filter {
    private static Logger.ALogger logger = Logger.of(JwtFilter.class);
    private static final String ERR_AUTHORIZATION_HEADER = "ERR_AUTHORIZATION_HEADER";
    private JwtCenter jwtCenter;

    private String jwtFilterTag;
    private String headerAuthorization;
    private String bearer;

    @Inject
    public JwtFilter(Materializer mat, JwtCenter jwtCenter, Config config) {
        super(mat);
        this.jwtCenter = jwtCenter;

        jwtFilterTag = config.getString("receptnekem.jwt.filtertag");
        headerAuthorization = config.getString("receptnekem.jwt.header.authorization");
        bearer = config.getString("receptnekem.jwt.header.bearer");
    }

    @Override
    public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> nextFilter, Http.RequestHeader requestHeader) {
        if (requestHeader.attrs().containsKey(Router.Attrs.HANDLER_DEF)) {
            HandlerDef handler = requestHeader.attrs().get(Router.Attrs.HANDLER_DEF);
            List<String> modifiers = handler.getModifiers();

            if (!modifiers.contains(jwtFilterTag)) {
                return nextFilter.apply(requestHeader);
            }
        }

        Optional<String> authHeader = requestHeader.getHeaders().get(headerAuthorization);

        if (!authHeader.filter(ah -> ah.contains(bearer)).isPresent()) {
            logger.error("f=JwtFilter, error=authHeaderNotPresent");
            return CompletableFuture.completedFuture(forbidden(ERR_AUTHORIZATION_HEADER));
        }

        String token = authHeader.map(ah -> ah.replace(bearer, "")).orElse("");
        Either<JwtCenter.Error, VerifiedJwt> res = jwtCenter.verify(token);

        if (res.left.isPresent()) {
            return CompletableFuture.completedFuture(forbidden(res.left.get().toString()));
        }

        return nextFilter.apply(requestHeader.withAttrs(requestHeader.attrs().put(Attrs.VERIFIED_JWT, res.right.get())));
    }
}