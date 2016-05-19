package main.java.server.json.request.user;

import org.jetbrains.annotations.NotNull;

/**
 * JSON to pass in for login 1
 */
public class ReqLogin1 {
    /**
     * Email of the user wishing to log in.
     */
    @NotNull
    public String email;

    public ReqLogin1(@NotNull String email) {
        this.email = email;
    }
}
