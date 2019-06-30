package models.repositories;

import play.db.jpa.JPAApi;

import javax.persistence.EntityManager;
import java.util.function.Function;

public abstract class JPARepositoryBase {
    protected final JPAApi jpaApi;

    public JPARepositoryBase(JPAApi jpaApi) {
        this.jpaApi = jpaApi;
    }

    protected <T> T wrap(Function<EntityManager, T> function) {
        return jpaApi.withTransaction(function);
    }
}
