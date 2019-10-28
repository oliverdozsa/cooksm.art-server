package models.repositories;

import dto.UserCreateUpdateDto;
import models.entities.User;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<Long> createOrUpdate(UserCreateUpdateDto dto);
    CompletionStage<User> byId(Long id);
}
