package data.repositories.imp;

import data.entities.GlobalSearch;
import data.repositories.GlobalSearchRepository;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Query;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;

public class EbeanGlobalSearchRepository implements GlobalSearchRepository {
    private EbeanServer ebean;

    private static final Logger.ALogger logger = Logger.of(EbeanGlobalSearchRepository.class);

    @Inject
    public EbeanGlobalSearchRepository(EbeanConfig ebeanConfig) {
        this.ebean = Ebean.getServer(ebeanConfig.defaultServer());
    }

    @Override
    public List<GlobalSearch> all() {
        logger.info("all()");
        Query<GlobalSearch> query = ebean.createQuery(GlobalSearch.class);
        return query.findList();
    }
}
