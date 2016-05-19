package main.java.server.json.request.user;

import org.jetbrains.annotations.NotNull;

/**
 * JSON containing all the fields required to create a user
 */
public class ReqCreateUser {
    /**
     * The verification code
     */
    @NotNull
    public final String code;

    // -------------- Information --------------
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
     * A hash of the master password
     */
    @NotNull
    public final String hashedMasterPassword;

    /**
     * Salt used for hashing the master password
     */
    @NotNull
    public final String masterPasswordSalt;

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

    public ReqCreateUser(@NotNull String code, @NotNull String firstName, @NotNull String lastName,
                         @NotNull String hashedMasterPassword, @NotNull String masterPasswordSalt,
                         @NotNull String encKeyDerivationSalt, @NotNull String macKeyDerivationSalt,
                         @NotNull String publicKey, @NotNull String publicKeyMAC, @NotNull String encIv,
                         @NotNull String privateKey) {
        this.code = code;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedMasterPassword = hashedMasterPassword;
        this.masterPasswordSalt = masterPasswordSalt;
        this.encKeyDerivationSalt = encKeyDerivationSalt;
        this.macKeyDerivationSalt = macKeyDerivationSalt;
        this.publicKey = publicKey;
        this.publicKeyMAC = publicKeyMAC;
        this.encIv = encIv;
        this.privateKey = privateKey;
    }
}
