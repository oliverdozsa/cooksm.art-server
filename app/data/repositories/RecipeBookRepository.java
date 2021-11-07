package data.repositories;

import data.entities.RecipeBook;

import java.util.List;
import java.util.Optional;

public interface RecipeBookRepository {
    RecipeBook create(String name, Long userId);
    RecipeBook single(Long id, Long userId);
    Integer countOf(Long user);
    Optional<RecipeBook> byNameOfUser(Long userId, String name);
    List<RecipeBook> allOf(Long userId);
    void update(Long id, String name, Long userId);
    void delete(Long id, Long userId);
    void addRecipes(Long id, Long userId, List<Long> recipeIds);
    Integer futureCountOf(Long id, Long userId, List<Long> recipeIdsToAdd);
    void updateRecipes(Long id, Long userId, List<Long> recipeIds);
    void checkRecipeBooksOfUser(List<Long> recipeBookIds, Long userId);
    List<RecipeBook> byIds(List<Long> ids);
}
