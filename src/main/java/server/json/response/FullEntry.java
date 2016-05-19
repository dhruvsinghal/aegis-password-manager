package main.java.server.json.response;

import main.java.model.AegisEntry;
import org.jetbrains.annotations.NotNull;

/**
 * A JSON representing an entry
 */
public class FullEntry {
    /**
     * The primary key ID of this entry.
     */
    public final int id;

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
     * The password to the account encrypted by the team key
     */
    @NotNull
    public final String password;

    /**
     * the iv used to encrypt this password
     */
    @NotNull
    public final String iv;

    public FullEntry(AegisEntry entry) {
        this.id = entry.id;
        this.title = entry.entryInfo.title;
        this.username = entry.entryInfo.username;
        this.password = entry.entryInfo.password;
        this.iv = entry.entryInfo.iv;
    }

    public FullEntry(int id, @NotNull String title, @NotNull String username, @NotNull String password, @NotNull String iv) {
        this.id = id;
        this.title = title;
        this.username = username;
        this.password = password;
        this.iv = iv;
    }
}
