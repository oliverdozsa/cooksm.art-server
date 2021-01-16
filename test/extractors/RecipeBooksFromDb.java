package extractors;

import data.entities.RecipeBook;
import io.ebean.Ebean;
import io.ebean.SqlQuery;

public class RecipeBooksFromDb {
    public static int countOfRecipesInRecipeBook(Long id) {
        SqlQuery countQuery = Ebean.createSqlQuery(
                "SELECT count(*) FROM recipe_book_recipe " +
                        "WHERE recipe_book_id = " + id
        );

        return countQuery.findSingleAttribute(Integer.class);
    }
}
