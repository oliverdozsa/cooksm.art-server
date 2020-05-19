package data.entities;

import javax.persistence.*;

@Entity
@Table(name = "global_search")
public class GlobalSearch {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne
    @JoinColumn(name = "search_id")
    private RecipeSearch search;

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

    public RecipeSearch getSearch() {
        return search;
    }

    public void setSearch(RecipeSearch search) {
        this.search = search;
    }
}
