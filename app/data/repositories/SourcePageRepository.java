package data.repositories;

import data.entities.SourcePage;
import lombokized.repositories.Page;

public interface SourcePageRepository {
    Page<SourcePage> allSourcePages();
}
