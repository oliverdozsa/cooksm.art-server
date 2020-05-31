package services;

import data.repositories.UserSearchRepository;
import lombokized.dto.UserSearchDto;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class UserSearchService {
    private UserSearchRepository userSearchRepository;

    @Inject
    public UserSearchService(UserSearchRepository userSearchRepository) {
        this.userSearchRepository = userSearchRepository;
    }

    public CompletionStage<List<UserSearchDto>> all(Long userId) {
        return userSearchRepository.all(userId)
                .thenApplyAsync(l -> l.stream().map(DtoMapper::toDto)
                        .collect(Collectors.toList()));
    }
}
