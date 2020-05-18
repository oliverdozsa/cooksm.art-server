package data.repositories;

import data.entities.SourcePage;
import lombokized.repositories.Page;

import java.util.concurrent.CompletionStage;

public interface SourcePageRepository {
    CompletionStage<Page<SourcePage>> allSourcePages();
}
