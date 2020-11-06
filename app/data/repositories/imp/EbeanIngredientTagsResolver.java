package data.repositories.imp;

import data.entities.Ingredient;
import data.entities.IngredientTag;
import io.ebean.EbeanServer;
import io.ebean.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class EbeanIngredientTagsResolver {
    private EbeanServer ebean;
    private Long userId;

    public EbeanIngredientTagsResolver(EbeanServer ebean, Long userId) {
        this.ebean = ebean;
        this.userId = userId;
    }

    public List<Long> resolve(List<Long> ingredientTagIds) {
        List<Long> result = new ArrayList<>();
        if (ingredientTagIds != null && ingredientTagIds.size() > 0) {
            Query<IngredientTag> query = ebean.createQuery(IngredientTag.class);

            query.where()
                    .in("id", ingredientTagIds);
            filterByUserIfNeeded(query);

            query.findList()
                    .forEach(tag -> result.addAll(getIngredientIdsOfTag(tag)));

        }

        return result;
    }

    private void filterByUserIfNeeded(Query<IngredientTag> query) {
        if (userId != null) {
            query.where()
                    .and()
                    .or()
                    .isNull("user.id")
                    .eq("user.id", userId)
                    .endOr()
                    .endAnd();
        } else {
            query.where()
                    .isNull("user.id");
        }
    }

    private List<Long> getIngredientIdsOfTag(IngredientTag tag) {
        return tag.getIngredients()
                .stream()
                .map(Ingredient::getId)
                .collect(Collectors.toList());
    }
}
