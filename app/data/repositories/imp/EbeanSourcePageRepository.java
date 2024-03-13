package data.repositories.imp;

import data.DatabaseExecutionContext;
import data.entities.SourcePage;
import data.repositories.SourcePageRepository;
import io.ebean.EbeanServer;
import io.ebean.Query;
import lombokized.repositories.Page;
import play.Logger;

import javax.inject.Inject;

public class EbeanSourcePageRepository implements SourcePageRepository {
    private EbeanServer ebean;
    private DatabaseExecutionContext executionContext;

    private static final Logger.ALogger logger = Logger.of(EbeanSourcePageRepository.class);

    @Inject
    public EbeanSourcePageRepository(EbeanServer ebean, DatabaseExecutionContext executionContext) {
        this.ebean = ebean;
        this.executionContext = executionContext;
    }

    @Override
    public Page<SourcePage> allSourcePages() {
        logger.info("allSourcePages()");
        Query<SourcePage> q = ebean.find(SourcePage.class);
        return new Page<>(q.findList(), q.findCount());
    }
}
