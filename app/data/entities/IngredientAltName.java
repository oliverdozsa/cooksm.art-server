package data.entities;

import javax.persistence.*;

// An ingredient's alternative name.
@Entity
@Table(name = "ingredient_alt_name")
public class IngredientAltName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ingredient_name_id")
    private IngredientName ingredientName;

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

    public IngredientName getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(IngredientName ingredientName) {
        this.ingredientName = ingredientName;
    }
}
