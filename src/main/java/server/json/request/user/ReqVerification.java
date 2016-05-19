package main.java.server.json.request.user;

import org.jetbrains.annotations.NotNull;

/**
 * JSON for sending a verification email
 */
public class ReqVerification {
    /**
     * The email of this user
     */
    @NotNull
    public final String email;

    public ReqVerification(@NotNull String email) {
        this.email = email;
    }
}
