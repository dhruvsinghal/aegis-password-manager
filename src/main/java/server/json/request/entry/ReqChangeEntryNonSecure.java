package main.java.server.json.request.entry;

import org.jetbrains.annotations.Nullable;

/**
 * JSON for changing the non-secure fields of an entry
 */
public class ReqChangeEntryNonSecure {
    /**
     * The title of this entry e.g. Facebook, Gmail, etc.
     */
    @Nullable
    public final String title;

    /**
     * The user name to this entry's account
     */
    @Nullable
    public final String username;

    public ReqChangeEntryNonSecure(@Nullable String title, @Nullable String username) {
        this.title = title;
        this.username = username;
    }
}
