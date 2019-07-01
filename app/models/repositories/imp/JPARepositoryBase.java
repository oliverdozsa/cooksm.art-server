package models.repositories.imp;

import play.db.jpa.JPAApi;

import javax.persistence.EntityManager;
import java.util.function.Function;

/**
 * Super class for common operations for JPA repositories.
 */
public abstract class JPARepositoryBase {
    protected final JPAApi jpaApi;

    public JPARepositoryBase(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    /**
     * Wraps the given function in a JPA transaction.
     *
     * @param function The function to wrap.
     * @param <T>      Result type.
     * @return Result of the function.
     */
    protected <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
