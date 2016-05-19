package main.java.model.info;

import main.java.server.json.request.user.ReqUpdateMasterPassword;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * All the extra information associated with a user
 */
public class UserInfo implements Serializable {
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


    public UserInfo withFirstNameAndLastName(@NotNull String firstName, @NotNull String lastName) {
        return new UserInfo(firstName, lastName, hashedMasterPassword,
                masterPasswordSalt, encKeyDerivationSalt, macKeyDerivationSalt,
                publicKey, publicKeyMAC, encIv, privateKey);
    }

    public UserInfo withMasterPasswordUpdate(@NotNull ReqUpdateMasterPassword json) {
        return new UserInfo(firstName, lastName, json.hashedMasterPassword, json.masterPasswordSalt, json.encKeyDerivationSalt,
                json.macKeyDerivationSalt, publicKey, json.publicKeyMAC, json.encIv, json.privateKey);
    }

    /**
     * This method is really only for testing. Do not use normally.
     */
    public UserInfo withPublicKey(@NotNull String publicKey){
        return new UserInfo(firstName, lastName, hashedMasterPassword,
                masterPasswordSalt, encKeyDerivationSalt, macKeyDerivationSalt,
                publicKey, publicKeyMAC, encIv, privateKey);
    }

    //Auto-generated constructor and methods. If you change the fields you need to regenerate the below
    public UserInfo(@NotNull String firstName, @NotNull String lastName, @NotNull String hashedMasterPassword,
                    @NotNull String masterPasswordSalt, @NotNull String encKeyDerivationSalt,
                    @NotNull String macKeyDerivationSalt, @NotNull String publicKey, @NotNull String publicKeyMAC,
                    @NotNull String encIv, @NotNull String privateKey) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (!firstName.equals(userInfo.firstName)) return false;
        if (!lastName.equals(userInfo.lastName)) return false;
        if (!hashedMasterPassword.equals(userInfo.hashedMasterPassword)) return false;
        if (!masterPasswordSalt.equals(userInfo.masterPasswordSalt)) return false;
        if (!encKeyDerivationSalt.equals(userInfo.encKeyDerivationSalt)) return false;
        if (!macKeyDerivationSalt.equals(userInfo.macKeyDerivationSalt)) return false;
        if (!publicKey.equals(userInfo.publicKey)) return false;
        if (!publicKeyMAC.equals(userInfo.publicKeyMAC)) return false;
        if (!encIv.equals(userInfo.encIv)) return false;
        return privateKey.equals(userInfo.privateKey);

    }

    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + hashedMasterPassword.hashCode();
        result = 31 * result + masterPasswordSalt.hashCode();
        result = 31 * result + encKeyDerivationSalt.hashCode();
        result = 31 * result + macKeyDerivationSalt.hashCode();
        result = 31 * result + publicKey.hashCode();
        result = 31 * result + publicKeyMAC.hashCode();
        result = 31 * result + encIv.hashCode();
        result = 31 * result + privateKey.hashCode();
        return result;
    }
}
