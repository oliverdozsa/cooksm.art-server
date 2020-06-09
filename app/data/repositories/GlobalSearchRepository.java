package data.repositories;

import lombokized.repositories.Page;
import data.entities.GlobalSearch;

import java.util.List;
import java.util.concurrent.CompletionStage;

public interface GlobalSearchRepository {
    CompletionStage<List<GlobalSearch>> all();
}
