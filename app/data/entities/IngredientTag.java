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

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinTable(name = "ingredient_tag_ingredient",
            joinColumns = {
                    @JoinColumn(name = "ingredient_tag_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "ingredient_id", referencedColumnName = "id")
            })
    private List<Ingredient> ingredients;

    @OneToMany(mappedBy = "tag", cascade = {CascadeType.REMOVE, CascadeType.PERSIST})
    private List<IngredientTagName> names;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<IngredientTagName> getNames() {
        return names;
    }

    public void setNames(List<IngredientTagName> names) {
        this.names = names;
    }
}
