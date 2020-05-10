package lombokized.dto;

import lombok.ToString;
import play.data.validation.Constraints;

import javax.validation.constraints.NotNull;

@ToString
public class UserSocialLoginDto {
    @NotNull
    @ToString.Exclude
    private String token;

    public UserSocialLoginDto(@NotNull String token) {
        this.token = token;
    }

    public UserSocialLoginDto() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
