package main.java.server.json.request.entry;

import org.jetbrains.annotations.NotNull;

/**
 * JSON for changing the password of an entry
 */
public class ReqCreateEntry {
    /**
     * The title of this entry e.g. Facebook, Gmail, etc.
     */
    @NotNull
    public final String title;

    /**
     * The user name to this entry's account
     */
    @NotNull
    public final String username;
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

    public ReqCreateEntry(@NotNull String title, @NotNull String username, @NotNull String password, @NotNull String iv) {
        this.title = title;
        this.username = username;
        this.password = password;
        this.iv = iv;
    }
}
