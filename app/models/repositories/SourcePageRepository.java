package models.repositories;

import lombokized.repositories.Page;
import models.entities.SourcePage;

import java.util.concurrent.CompletionStage;

public interface SourcePageRepository {
    CompletionStage<Page<SourcePage>> allSourcePages();
}
