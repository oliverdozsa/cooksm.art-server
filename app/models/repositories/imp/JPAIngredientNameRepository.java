package models.repositories.imp;

import models.DatabaseExecutionContext;
import models.entities.IngredientName;
import models.repositories.IngredientNameRepository;
import models.repositories.RepositoryBase;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class JPAIngredientNameRepository extends RepositoryBase implements IngredientNameRepository {
    private final DatabaseExecutionContext dbExecutionContext;

    @Inject
    public JPAIngredientNameRepository(JPAApi jpaApi, DatabaseExecutionContext dbExecutionContext) {
        super(jpaApi);
        this.dbExecutionContext = dbExecutionContext;
    }

    @Override
    public CompletionStage<Stream<IngredientName>> list(String nameLike, Long languageId, int limit, int offset) {
        return supplyAsync(() -> wrap(em -> list(em, nameLike, languageId, limit, offset)), dbExecutionContext);
    }

    @Override
    public CompletionStage<Long> count(String nameLike, Long languageId) {
        return supplyAsync(() -> wrap(em -> count(em, nameLike, languageId)), dbExecutionContext);
    }

    private Stream<IngredientName> list(EntityManager em, String nameLike, Long languageId, int limit, int offset) {
        TypedQuery<IngredientName> query =
                em.createNamedQuery(IngredientName.NQ_LIST_INGREDIENT_NAMES, IngredientName.class);
        query.setParameter("nameLike", "%" + nameLike + "%");
        query.setParameter("languageId", languageId);
        query.setMaxResults(limit);
        query.setFirstResult(offset);

        return query.getResultList().stream();
    }

    private Long count(EntityManager em, String nameLike, Long languageId){
        TypedQuery<Long> query =
                em.createNamedQuery(IngredientName.NQ_LIST_INGREDIENT_NAMES_COUNT, Long.class);
        query.setParameter("nameLike", "%" + nameLike + "%");
        query.setParameter("languageId", languageId);

        return query.getSingleResult();
    }
}
