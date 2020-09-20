package data.repositories;

import lombokized.dto.UserCreateUpdateDto;
import data.entities.User;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<User> createOrUpdate(UserCreateUpdateDto dto);
    CompletionStage<User> byId(Long id);
    CompletionStage<Void> delete(Long id);
}
