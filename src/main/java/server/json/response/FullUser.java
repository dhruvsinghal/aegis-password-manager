package main.java.server.json.response;

import main.java.model.AegisUser;
import org.jetbrains.annotations.NotNull;

/**
 * JSON to pass for the full user info. In general, can only get this if you are this user.
 */
public class FullUser {
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
     * The user's lastname
     */
    @NotNull
    public final String lastName;

    // -------------- Security --------------
    /**
     * Salt used for generating an encryption key with the master password
     */
    @NotNull
    public final String encKeyDerivationSalt;

    /**
     * Salt used for generating a mac key with the master password
     */
    @NotNull
    public final String macKeyDerivationSalt;

    /**
     * Unencrypted version of this users RSA public key
     */
    @NotNull
    public final String publicKey;

    /**
     * The MAC of the public key (MAC by the master password)
     */
    @NotNull
    public final String publicKeyMAC;

    /**
     * IV used to encrypt the user's private key
     */
    @NotNull
    public final String encIv;

    /**
     * Encrypted version of this users RSA private key (encrypted by the master key)
     */
    @NotNull
    public final String privateKey;

    /**
     * Create a full user from an Aegis User.
     */
    public FullUser(AegisUser user) {
        this.id = user.id;
        this.firstName = user.userInfo.firstName;
        this.lastName = user.userInfo.lastName;
        this.encKeyDerivationSalt = user.userInfo.encKeyDerivationSalt;
        this.macKeyDerivationSalt = user.userInfo.macKeyDerivationSalt;
        this.publicKey = user.userInfo.publicKey;
        this.publicKeyMAC = user.userInfo.publicKeyMAC;
        this.encIv = user.userInfo.encIv;
        this.privateKey = user.userInfo.privateKey;
    }
}
