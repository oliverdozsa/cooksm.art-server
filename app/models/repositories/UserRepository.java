package models.repositories;

import dto.UserCreateUpdateDto;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<Long> create(UserCreateUpdateDto dto);
    CompletionStage<Void> update(Long id, UserCreateUpdateDto dto);
}
