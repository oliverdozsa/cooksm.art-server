package data.repositories;

import lombokized.repositories.Page;
import data.entities.GlobalSearch;

import java.util.concurrent.CompletionStage;

public interface GlobalSearchRepository {
    CompletionStage<Page<GlobalSearch>> all();
}
