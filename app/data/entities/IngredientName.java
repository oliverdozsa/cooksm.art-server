package data.entities;

import javax.persistence.*;

/**
 * Ingredient name entity.
 */
@Entity
@Table(name = "ingredient_name")
public class IngredientName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @JoinColumn(name = "ingredient_id")
    @ManyToOne
    private Ingredient ingredient;

    // Language of the name.
    @JoinColumn(name = "language_id")
    @ManyToOne
    private Language language;

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

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
