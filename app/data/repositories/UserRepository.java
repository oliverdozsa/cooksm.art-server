package data.repositories;

import lombokized.dto.UserCreateUpdateDto;
import data.entities.User;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    User createOrUpdate(UserCreateUpdateDto dto);
    User byId(Long id);
    void delete(Long id);
}
