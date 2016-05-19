package main.java.server.json.response;

import main.java.model.AegisUser;
import org.jetbrains.annotations.NotNull;

/**
 * JSON with the partial user information.
 */
public class PartialUser {
    // -------------- Information --------------
    /**
     * user id
     */
    public final int id;

    /**
     * The user's firstname
     */
    @NotNull
    public final String firstName;

    /**
     * The user's email
     */
    @NotNull
    public final String email;


    /**
     * The user's lastname
     */
    @NotNull
    public final String lastName;

    /**
     * Unencrypted version of this users RSA public key
     */
    @NotNull
    public final String publicKey;

    /**
     * Create a partial user from an Aegis User.
     */
    public PartialUser(AegisUser user) {
        this.id = user.id;
        this.firstName = user.userInfo.firstName;
        this.lastName = user.userInfo.lastName;
        this.publicKey = user.userInfo.publicKey;
        this.email = user.email;
    }
}
