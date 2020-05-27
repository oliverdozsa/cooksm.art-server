package data.repositories;

import lombokized.dto.UserCreateUpdateDto;
import data.entities.User;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<Long> createOrUpdate(UserCreateUpdateDto dto);
    CompletionStage<User> byId(Long id);
}