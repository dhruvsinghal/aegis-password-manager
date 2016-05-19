package main.java.server.json.request.entry;

import org.jetbrains.annotations.NotNull;

/**
 * JSON for changing the password of an entry
 */
public class ReqChangeEntrySecure {
    /**
     * This is the password for the entry which is encrypted by the team password.
     */
    @NotNull
    public final String password;

    /**
     * The IV used to encrypt this password.
     */
    @NotNull
    public final String iv;

    public ReqChangeEntrySecure(@NotNull String password, @NotNull String iv) {
        this.password = password;
        this.iv = iv;
    }
}
