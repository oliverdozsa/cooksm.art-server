package models.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import models.DatabaseExecutionContext;
import models.entities.IngredientName;
import models.repositories.IngredientNameRepository;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
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
}
