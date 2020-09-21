package data.entities;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "recipe")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "name")
    private String name;

    @Lob
    @Column(name = "url")
    private String url;

    @Column(name = "date_added")
    private Instant dateAdded;

    @Column(name = "numofings")
    private Integer numofings;

    @Column(name = "time")
    private Integer time;

    @OneToMany(mappedBy = "recipe", fetch = FetchType.EAGER)
    private List<RecipeIngredient> ingredients;

    @OneToMany(mappedBy = "recipe")
    private List<RecipeDescription> descriptions;

    @ManyToOne
    @JoinColumn(name = "source_page_id")
    private SourcePage sourcePage;

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Instant dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Integer getNumofings() {
        return numofings;
    }

    public void setNumofings(Integer numofings) {
        this.numofings = numofings;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<RecipeDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<RecipeDescription> descriptions) {
        this.descriptions = descriptions;
    }

    public SourcePage getSourcePage() {
        return sourcePage;
    }

    public void setSourcePage(SourcePage sourcePage) {
        this.sourcePage = sourcePage;
    }
}
