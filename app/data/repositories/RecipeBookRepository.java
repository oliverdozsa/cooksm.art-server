package data.repositories;

import data.entities.RecipeBook;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface RecipeBookRepository {
    CompletionStage<RecipeBook> create(String name, Long userId);
    CompletionStage<RecipeBook> single(Long id, Long userId);
    CompletionStage<Integer> countOf(Long user);
    CompletionStage<Optional<RecipeBook>> byNameOfUser(Long userId, String name);
    CompletionStage<List<RecipeBook>> allOf(Long userId);
    CompletionStage<Void> update(Long id, String name, Long userId);
    CompletionStage<Void> delete(Long id, Long userId);
    CompletionStage<Void> addRecipes(Long id, Long userId, List<Long> recipeIds);
    CompletionStage<Integer> futureCountOf(Long id, Long userId, List<Long> recipeIdsToAdd);
    CompletionStage<Void> updateRecipes(Long id, Long userId, List<Long> recipeIds);
    CompletionStage<Void> checkRecipeBooksOfUser(List<Long> recipeBookIds, Long userId);
}
