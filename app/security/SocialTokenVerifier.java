package security;

public interface SocialTokenVerifier {
    boolean verify(String token);
}
