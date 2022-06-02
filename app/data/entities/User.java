package data.entities;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "emailConstraint", columnNames = "email")
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "last_update")
    private Instant lastUpdate;

    @Column(name = "google_user_id")
    private String googleUserId;

    @Column(name = "facebook_user_id")
    private String facebookUserId;

    @Column(name = "picture")
    private String picture;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<FavoriteRecipe> favoriteRecipes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<UserSearch> userSearches;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<RecipeBook> recipeBooks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<ShoppingList> shoppingLists;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private List<IngredientTag> ingredientTags;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGoogleUserId() {
        return googleUserId;
    }

    public void setGoogleUserId(String googleUserId) {
        this.googleUserId = googleUserId;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
