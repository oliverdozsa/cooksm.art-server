package dto;

import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.*;

@Constraints.Validate
public class MenuCreateUpdateDto implements Constraints.Validatable<ValidationError> {
    @Constraints.Required
    @Constraints.MinLength(2)
    public String name;

    @Constraints.Required
    public List<Item> items;

    @Override
    public String toString() {
        return "MenuCreateUpdateDto{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }

    @Override
    public ValidationError validate() {
        Map<Integer, Set<Integer>> ordersByGroups = new HashMap<>();

        for (Item item : items) {
            if (isOrderAlreadyPresentForGroup(item.group, item.order, ordersByGroups)) {
                return new ValidationError("", "Duplicate group and order present!");
            }

            if (!ordersByGroups.containsKey(item.group)) {
                ordersByGroups.put(item.group, new HashSet<>());
            }
            ordersByGroups.get(item.group).add(item.order);
        }

        return null;
    }

    private boolean isOrderAlreadyPresentForGroup(Integer group, Integer order, Map<Integer, Set<Integer>> ordersByGroups) {
        if (!ordersByGroups.containsKey(group)) {
            return false;
        }

        return ordersByGroups.get(group).contains(order);
    }

    public static class Item {
        @Constraints.Required
        public Long recipeId;

        @Constraints.Required
        @Constraints.Min(1)
        public Integer group;

        @Constraints.Required
        @Constraints.Min(1)
        public Integer order;

        @Override
        public String toString() {
            return "Item{" +
                    "recipeId=" + recipeId +
                    ", group=" + group +
                    ", order=" + order +
                    '}';
        }
    }
}
