package data.entities;

import javax.persistence.*;

@Entity
@Table(name = "ingredient_tag_name")
public class IngredientTagName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private IngredientTag tag;

    @ManyToOne
    @JoinColumn(name = "language_id")
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

    public IngredientTag getTag() {
        return tag;
    }

    public void setTag(IngredientTag tag) {
        this.tag = tag;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}
