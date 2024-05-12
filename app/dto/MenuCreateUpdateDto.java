package dto;

import play.data.validation.Constraints;

import java.util.*;

public class MenuCreateUpdateDto {
    @Constraints.Required
    @Constraints.MinLength(2)
    public String name;

    @Constraints.Required
    public List<Group> groups;

    @Override
    public String toString() {
        return "MenuCreateUpdateDto{" +
                "name='" + name + '\'' +
                ", groups=" + groups +
                '}';
    }

    public static class Group {
        @Constraints.Required
        public List<Long> recipes;

        @Override
        public String toString() {
            return "Group{" +
                    "recipes=" + recipes +
                    '}';
        }
    }
}
