package models.entities;

import javax.persistence.*;

@Entity
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

    @JoinColumn(name = "language_id")
    @ManyToOne
    private Language language;
}
