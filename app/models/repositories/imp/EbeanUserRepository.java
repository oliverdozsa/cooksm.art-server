package models.repositories.imp;

import dto.UserCreateUpdateDto;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import models.DatabaseExecutionContext;
import models.repositories.UserRepository;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class EbeanUserRepository implements UserRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    @Inject
    public EbeanUserRepository(EbeanConfig ebeanConfig, DatabaseExecutionContext executionContext) {
        this.ebean = Ebean.getServer(ebeanConfig.defaultServer());
        this.executionContext = executionContext;
    }

    @Override
    public CompletionStage<Long> create(UserCreateUpdateDto dto) {
        return supplyAsync(() -> {
            return null;
        }, executionContext);
    }

    @Override
    public CompletionStage<Void> update(Long id, UserCreateUpdateDto dto) {
        return supplyAsync(() -> {
            return null;
        }, executionContext);
    }
}
