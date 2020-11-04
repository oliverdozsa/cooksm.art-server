package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.IngredientTag;
import data.repositories.IngredientTagRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.IngredientTagRepositoryParams;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
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
    public CompletionStage<Page<IngredientTag>> page(IngredientTagRepositoryParams.Page params) {
        return supplyAsync(() -> {
            Query<IngredientTag> query = ebean.createQuery(IngredientTag.class);
            query.where().ilike("name", "%" + params.getNameLike() + "%");
            query.where().eq("language.id", params.getLanguageId());
            query.setFirstRow(params.getOffset());
            query.setMaxRows(params.getLimit());

            if (params.getUserId() != null) {
                query.where().eq("user.id", params.getUserId());
            }

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }

    @Override
    public CompletionStage<List<IngredientTag>> byIds(List<Long> ids) {
        return supplyAsync(() -> {
            List<IngredientTag> tags = new ArrayList<>();
            ids.forEach(id -> {
                IngredientTag entity = ebean.createQuery(IngredientTag.class)
                        .where()
                        .isNull("user.id")
                        .eq("id", id)
                        .findOne();
                if (entity == null) {
                    throw new IllegalArgumentException("ID is not valid. ID = " + id);
                }

                tags.add(entity);
            });
            return tags;
        }, executionContext);
    }
}
