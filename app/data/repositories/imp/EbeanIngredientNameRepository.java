package data.repositories.imp;

import data.entities.Ingredient;
import data.entities.IngredientName;
import data.repositories.IngredientNameRepository;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class EbeanIngredientNameRepository implements IngredientNameRepository {
    private EbeanServer ebean;

    private static final Logger.ALogger logger = Logger.of(EbeanIngredientNameRepository.class);

    @Inject
    public EbeanIngredientNameRepository(EbeanServer ebean) {
        this.ebean = ebean;
    }

    @Override
    public Page<IngredientName> page(String nameLike, Long languageId, int limit, int offset) {
        logger.info("page(): nameLike = {}, languageId = {}, limit = {}, offset = {}",
                nameLike, languageId, limit, offset);
        Query<IngredientName> query = ebean.createQuery(IngredientName.class);

        query.where()
                .or()
                .ilike("name", "%" + nameLike + "%")
                .ilike("altNames.name", "%" + nameLike + "%")
                .endOr();
        query.where().eq("language.id", languageId);

        query.setFirstRow(offset);
        query.setMaxRows(limit);
        query.orderBy("relevanceScore desc");

        return new Page<>(query.findList(), query.findCount());
    }

    @Override
    public List<IngredientName> byIngredientIds(List<Long> ingredientIds, Long languageId) {
        List<IngredientName> names = new ArrayList<>();
        logger.info("byIngredientIds(): ids = {}, languageId = {}", ingredientIds.toString(), languageId);
        ingredientIds.forEach(id -> {
            Ingredient ingredient = ebean.find(Ingredient.class, id);

            if (ingredient == null) {
                logger.warn("byIngredientIds(): ID is not valid. ID = " + id);
                throw new IllegalArgumentException("ID is not valid. ID = " + id);
            }

            IngredientName ingredientName = ingredient.getNames().stream()
                    .filter(n -> n.getLanguage().getId().equals(languageId))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Not found ingredient name with language id. ingredient ID = " + id + ", languageId = " + languageId)
                    );

            names.add(ingredientName);
        });

        return names;
    }
}
