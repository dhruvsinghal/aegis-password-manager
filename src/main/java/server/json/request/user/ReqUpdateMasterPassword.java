package main.java.server.json.request.user;

import org.jetbrains.annotations.NotNull;

/**
 * JSON for an update user's master password request. Users can only update themselves.
 * <p>
 * This is essentially all the fields that will change due to a master password update
 */
public class ReqUpdateMasterPassword {
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
     * The MAC of the public key (MAC by the master password)
     */
    @NotNull
    public final String publicKeyMAC;

    /**
     * Encrypted version of this users RSA private key (encrypted by the master key with the same iv)
     */
    @NotNull
    public final String privateKey;

    /**
     * IV used to encrypt the user's private key
     */
    @NotNull
    public final String encIv;

    public ReqUpdateMasterPassword(@NotNull String hashedMasterPassword, @NotNull String masterPasswordSalt,
                                   @NotNull String encKeyDerivationSalt, @NotNull String macKeyDerivationSalt,
                                   @NotNull String publicKeyMAC, @NotNull String privateKey, @NotNull String encIv) {
        this.hashedMasterPassword = hashedMasterPassword;
        this.masterPasswordSalt = masterPasswordSalt;
        this.encKeyDerivationSalt = encKeyDerivationSalt;
        this.macKeyDerivationSalt = macKeyDerivationSalt;
        this.publicKeyMAC = publicKeyMAC;
        this.privateKey = privateKey;
        this.encIv = encIv;
    }
}
