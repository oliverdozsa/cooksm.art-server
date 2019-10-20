package models.repositories;

import dto.UserCreateUpdateDto;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<String> create(UserCreateUpdateDto dto);
    CompletionStage<Void> update(UserCreateUpdateDto dto);
}
