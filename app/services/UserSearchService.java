package services;

import com.typesafe.config.Config;
import data.entities.UserSearch;
import data.repositories.UserSearchRepository;
import data.repositories.exceptions.ForbiddenExeption;
import dto.UserSearchCreateUpdateDto;
import lombokized.dto.UserSearchDto;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class UserSearchService {
    private UserSearchRepository userSearchRepository;
    private RecipeSearchService recipeSearchService;
    private Integer maxPerUser;

    @Inject
    public UserSearchService(UserSearchRepository userSearchRepository, RecipeSearchService recipeSearchService, Config config) {
        this.userSearchRepository = userSearchRepository;
        this.recipeSearchService = recipeSearchService;
        maxPerUser = config.getInt("receptnekem.usersearches.maxperuser");
    }

    public CompletionStage<List<UserSearchDto>> all(Long userId) {
        return userSearchRepository.all(userId)
                .thenApplyAsync(l -> l.stream().map(DtoMapper::toDto)
                        .collect(Collectors.toList()));
    }

    public CompletionStage<Long> create(UserSearchCreateUpdateDto dto, Long userId) {
        return userSearchRepository.count(userId).thenAcceptAsync(c -> {
            if (c >= maxPerUser) {
                throw new ForbiddenExeption("User reached max limit! userId = " + userId);
            }
        }).thenComposeAsync(v -> recipeSearchService.createWithLongId(dto.query, true))
                .thenComposeAsync(searchId -> userSearchRepository.create(dto.name, userId, searchId))
                .thenApply(UserSearch::getId);

    }

    public CompletionStage<UserSearchDto> single(Long id, Long userId) {
        return userSearchRepository.single(id, userId)
                .thenApplyAsync(DtoMapper::toDto);
    }

    public CompletionStage<Boolean> delete(Long id, Long userId) {
        return userSearchRepository.delete(id, userId);
    }

    public CompletionStage<Void> update(Long id, Long userId, UserSearchCreateUpdateDto dto){
        return userSearchRepository.update(dto.name, userId, id)
                .thenApplyAsync(entity -> entity.getSearch().getId())
                .thenComposeAsync(searchId -> recipeSearchService.update(dto.query, true, searchId));
    }
}
