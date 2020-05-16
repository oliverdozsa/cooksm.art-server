package models.repositories;

import lombokized.repositories.Page;
import models.entities.GlobalSearch;

import java.util.concurrent.CompletionStage;

public interface GlobalSearchRepository {
    CompletionStage<Page<GlobalSearch>> page(int limit, int offset);
}
