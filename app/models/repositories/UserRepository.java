package models.repositories;

import dto.UserCreateUpdateDto;

import java.util.concurrent.CompletionStage;

public interface UserRepository {
    CompletionStage<Long> create(UserCreateUpdateDto dto);
    CompletionStage<Void> update(UserCreateUpdateDto dto);
    CompletionStage<Boolean> doesExist(String email);
}
