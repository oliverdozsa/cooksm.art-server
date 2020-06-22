package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.IngredientTag;
import data.repositories.IngredientTagRepository;
import data.repositories.exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Override
    public CompletionStage<List<IngredientTag>> byIds(List<Long> ids) {
        return supplyAsync(() -> {
            List<IngredientTag> tags = new ArrayList<>();
            ids.forEach(id -> {
                IngredientTag entity = ebean.find(IngredientTag.class, id);
                if (entity == null) {
                    throw new IllegalArgumentException("ID is not valid. ID = " + id);
                }

                tags.add(entity);
            });
            return tags;
        }, executionContext);
    }

    @Override
    public CompletionStage<IngredientTag> single(Long id) {
        return supplyAsync(() -> {
            IngredientTag entity = ebean.find(IngredientTag.class, id);
            if (entity == null) {
                throw new NotFoundException("Not found tag with id = " + id);
            }

            return entity;
        }, executionContext);
    }
}
