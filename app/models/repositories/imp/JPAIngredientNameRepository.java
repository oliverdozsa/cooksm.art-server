package models.repositories.imp;

import models.DatabaseExecutionContext;
import models.entities.IngredientName;
import models.entities.Language;
import models.repositories.IngredientNameRepository;
import models.repositories.Page;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class JPAIngredientNameRepository extends JPARepositoryBase implements IngredientNameRepository {
    private final DatabaseExecutionContext dbExecutionContext;

    @Inject
    public JPAIngredientNameRepository(JPAApi jpaApi, DatabaseExecutionContext dbExecutionContext) {
        super(jpaApi);
        this.dbExecutionContext = dbExecutionContext;
    }

    @Override
    public CompletionStage<Page<IngredientName>> page(String nameLike, Long languageId, int limit, int offset) {
        return supplyAsync(() -> wrap(em -> page(em, nameLike, languageId, limit, offset)), dbExecutionContext);
    }

    private Page<IngredientName> page(EntityManager em, String nameLike, Long languageId, int limit, int offset) {
        List<IngredientName> ingredientNames = pageList(em, nameLike, languageId, limit, offset);
        Long ingredientNamesCount = pageCount(em, nameLike, languageId);

        return new Page<>(ingredientNames, ingredientNamesCount);
    }

    private List<IngredientName> pageList(EntityManager em, String nameLike, Long languageId, int limit, int offset) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<IngredientName> criteriaQuery = criteriaBuilder.createQuery(IngredientName.class);
        Root<IngredientName> root = criteriaQuery.from(IngredientName.class);

        criteriaQuery.select(root)
                .where(pageCriteria(criteriaBuilder, root, nameLike, languageId));

        TypedQuery<IngredientName> typedQuery = em.createQuery(criteriaQuery);

        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

        return typedQuery.getResultList();
    }

    private Long pageCount(EntityManager em, String nameLike, Long languageId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<IngredientName> root = criteriaQuery.from(IngredientName.class);

        criteriaQuery.select(criteriaBuilder.count(root))
                .where(pageCriteria(criteriaBuilder, root, nameLike, languageId));

        TypedQuery<Long> typedQuery = em.createQuery(criteriaQuery);

        return typedQuery.getSingleResult();
    }

    private <T> Predicate pageCriteria(
            CriteriaBuilder criteriaBuilder, Root<T> root, String nameLike, Long languageId) {
        Join<IngredientName, Language> join = root.join("language");

        Predicate nameLikePredicate = criteriaBuilder.like(root.get("name"), "%" + nameLike + "%");
        Predicate languageEqPredice = criteriaBuilder.equal(join.get("id"), languageId);

        return criteriaBuilder.and(nameLikePredicate, languageEqPredice);
    }
}
