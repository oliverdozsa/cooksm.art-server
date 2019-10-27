package dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import play.data.validation.Constraints;

import javax.validation.constraints.NotNull;

public class UserSocialLoginDto {
    @Constraints.MinLength(3)
    private String fullName;

    @Constraints.Email
    private String email;

    @NotNull
    private String token;

    public UserSocialLoginDto(@Constraints.MinLength(3) String fullName, @Constraints.Email String email, @NotNull String token) {
        this.fullName = fullName;
        this.email = email;
        this.token = token;
    }

    public UserSocialLoginDto() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
