package services;

import data.repositories.UserSearchRepository;
import dto.UserSearchCreateDto;
import lombokized.dto.UserSearchDto;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class UserSearchService {
    private UserSearchRepository userSearchRepository;
    private RecipeSearchService recipeSearchService;

    @Inject
    public UserSearchService(UserSearchRepository userSearchRepository, RecipeSearchService recipeSearchService) {
        this.userSearchRepository = userSearchRepository;
        this.recipeSearchService = recipeSearchService;
    }

    public CompletionStage<List<UserSearchDto>> all(Long userId) {
        return userSearchRepository.all(userId)
                .thenApplyAsync(l -> l.stream().map(DtoMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public CompletionStage<Long> create(UserSearchCreateDto dto, Long userId) {
        return recipeSearchService.createWithLongId(dto.query, true)
                .thenComposeAsync(searchId -> userSearchRepository.create(dto.name, userId, searchId));
    }
}
