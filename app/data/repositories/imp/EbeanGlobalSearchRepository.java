package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.GlobalSearch;
import data.repositories.GlobalSearchRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanGlobalSearchRepository implements GlobalSearchRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    @Inject
    public EbeanGlobalSearchRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Page<GlobalSearch>> page(int limit, int offset) {
        return supplyAsync(() -> {
            Query<GlobalSearch> query = ebean.createQuery(GlobalSearch.class)
                    .setFirstRow(offset)
                    .setMaxRows(limit);

            return new Page<>(query.findList(), query.findCount());
        }, executionContext);
    }
}
