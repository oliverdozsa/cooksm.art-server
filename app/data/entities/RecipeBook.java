package data.entities;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "recipe_book")
public class RecipeBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "last_accessed")
    private Instant lastAccessed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(cascade = CascadeType.REMOVE)
    @JoinTable(name = "recipe_book_recipe",
            joinColumns = {
                    @JoinColumn(name = "recipe_book_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "recipe_id", referencedColumnName = "id")
            })
    private List<Recipe> recipes;

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

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Instant lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }
}
