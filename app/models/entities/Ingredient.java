package models.entities;

import io.ebean.Model;

import javax.persistence.*;
import java.util.List;

/**
 * Ingredient entity.
 */
@Entity
@Table(name = "ingredient")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(mappedBy = "ingredient")
    private List<IngredientName> names;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<IngredientName> getNames() {
        return names;
    }

    public void setNames(List<IngredientName> names) {
        this.names = names;
    }
}
