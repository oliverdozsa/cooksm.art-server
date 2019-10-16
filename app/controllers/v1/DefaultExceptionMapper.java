package controllers.v1;

import com.google.common.base.Function;
import models.repositories.exceptions.BusinessLogicViolationException;
import models.repositories.exceptions.NotFoundException;
import play.Logger;
import play.mvc.Result;

import static play.mvc.Results.*;


class DefaultExceptionMapper implements Function<Throwable, Result> {
    private Logger.ALogger logger;

    public DefaultExceptionMapper(Logger.ALogger logger) {
        this.logger = logger;
    }

    @Override
    public Result apply(Throwable input) {
        if (input instanceof IllegalArgumentException ||
                input instanceof BusinessLogicViolationException) {
            logger.warn("Bad Request!", input);
            return badRequest();
        }

        if (input instanceof NotFoundException) {
            logger.warn("Not Found!", input);
            return notFound();
        }

        logger.error("Internal Error!", input);
        return internalServerError();
    }
}
