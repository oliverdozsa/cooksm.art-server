package services;

import com.typesafe.config.Config;

import javax.inject.Inject;

public class LanguageService {
    private Long defaultLanguageId;

    @Inject
    public LanguageService(Config config) {
        defaultLanguageId = config.getLong("openrecipes.default.languageid");
    }

    public Long getLanguageIdOrDefault(Long id) {
        if (id == null || id == 0L) {
            return defaultLanguageId;
        }

        return id;
    }

    public Long getDefault() {
        return defaultLanguageId;
    }
}
