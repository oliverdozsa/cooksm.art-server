package data.entities;

import javax.persistence.*;

@Entity
@Table(name = "menu_item")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "menu_id")
    private Menu menu;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @Column(name = "order")
    private Integer order;

    @Column(name = "group")
    private Integer group;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }
}
