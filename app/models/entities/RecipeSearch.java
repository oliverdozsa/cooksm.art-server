package models.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recipe_search")
@SequenceGenerator(name = "recipe_search_seq", initialValue = 61 * 62 * 62 + 61 * 62 + 61)
public class RecipeSearch {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recipe_search_seq")
    private Long id;

    @Column(name = "query", nullable = false)
    @Lob
    private String query;

    @Column(name = "last_accessed")
    private Instant lastAccessed;

    @Column(name = "is_permanent")
    private boolean isPermanent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Instant getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Instant lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public boolean isPermanent() {
        return isPermanent;
    }

    public void setPermanent(boolean permanent) {
        isPermanent = permanent;
    }
}
