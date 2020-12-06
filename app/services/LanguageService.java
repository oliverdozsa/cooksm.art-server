package services;

import com.typesafe.config.Config;
import play.Logger;

import javax.inject.Inject;

public class LanguageService {
    private Long defaultLanguageId;

    private static final Logger.ALogger logger = Logger.of(LanguageService.class);

    @Inject
    public LanguageService(Config config) {
        defaultLanguageId = config.getLong("openrecipes.default.languageid");
    }

    public Long getLanguageIdOrDefault(Long id) {
        logger.info("getLanguageIdOrDefault(): id = {}", id);
        if (id == null || id == 0L) {
            return defaultLanguageId;
        }

        return id;
    }

    public Long getDefault() {
        return defaultLanguageId;
    }
}
