package main.java.server.json.response;

import org.jetbrains.annotations.NotNull;

/**
 * The response to a login1 request
 */
public class Salt {
    /**
     * Salt for the password hash
     */
    @NotNull
    public final String salt;

    public Salt(@NotNull String salt) {
        this.salt = salt;
    }
}
