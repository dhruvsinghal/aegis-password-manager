package main.java.server.json.request.user;

import org.jetbrains.annotations.Nullable;

/**
 * JSON for an update user request. Users can only update themselves.
 * <p>
 * Note that fields are nullable (this means they are essentially optional
 */
public class ReqUpdateUser {
    /**
     * The email of this user
     */
    @Nullable
    public final String email;

    /**
     * The user's firstname
     */
    @Nullable
    public final String firstName;

    /**
     * The user's lastname
     */
    @Nullable
    public final String lastName;

    public ReqUpdateUser(@Nullable String email, @Nullable String firstName, @Nullable String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
