package lombokized.security;

public interface VerifiedUserInfo {
    String getFullName();
    String getEmail();
    String getSocialId();

    String getPicture();
}
