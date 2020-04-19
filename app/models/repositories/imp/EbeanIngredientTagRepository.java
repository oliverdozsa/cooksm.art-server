package models.repositories.imp;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import models.DatabaseExecutionContext;
import models.entities.IngredientTag;
import models.repositories.IngredientTagRepository;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanIngredientTagRepository implements IngredientTagRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    @Inject
    public EbeanIngredientTagRepository(EbeanConfig config, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(config.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<IngredientTag>> page(String nameLike, Long languageId, int limit, int offset) {
        return supplyAsync(() -> {
            Query<IngredientTag> query = ebean.createQuery(IngredientTag.class);
            query.where().ilike("name", "%" + nameLike + "%");
            query.where().eq("language.id", languageId);
            query.setFirstRow(offset);
            query.setMaxRows(limit);

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }
}
