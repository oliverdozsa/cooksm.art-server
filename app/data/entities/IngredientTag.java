package data.entities;

import javax.persistence.*;
import java.util.List;

/**
 * Ingredient tag entity. An ingredient can have many tags, and one tag
 * can belong to many ingredients.
 */
@Entity
@Table(name = "ingredient_tag")
public class IngredientTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToMany
    @JoinTable(name = "ingredient_tag_ingredient",
            joinColumns = {
                    @JoinColumn(name = "ingredient_tag_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "ingredient_id", referencedColumnName = "id")
            })
    private List<Ingredient> ingredients;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
