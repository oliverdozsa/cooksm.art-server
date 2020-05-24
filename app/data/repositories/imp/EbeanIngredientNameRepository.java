package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.Ingredient;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import data.entities.IngredientName;
import data.repositories.IngredientNameRepository;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanIngredientNameRepository implements IngredientNameRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    @Inject
    public EbeanIngredientNameRepository(EbeanConfig config, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(config.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<IngredientName>> page(String nameLike, Long languageId, int limit, int offset) {
        return supplyAsync(() -> {
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
        }, executionContext);
    }

    @Override
    public CompletionStage<List<IngredientName>> byIngredientIds(List<Long> ids, Long languageId) {
        return supplyAsync(() -> {
            List<IngredientName> names = new ArrayList<>();
            ids.forEach(id -> {
                Ingredient ingredient = ebean.find(Ingredient.class, id);

                if (ingredient == null) {
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
        }, executionContext);
    }
}
