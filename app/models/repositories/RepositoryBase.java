package models.repositories;

import play.db.jpa.JPAApi;

import javax.persistence.EntityManager;
import java.util.function.Function;

public abstract class RepositoryBase {
    protected final JPAApi jpaApi;

    public RepositoryBase(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    protected <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
