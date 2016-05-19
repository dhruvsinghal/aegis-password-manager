package main.java.server.config;

import org.jetbrains.annotations.NotNull;

/**
 * A object representing the configuration of our server. This will get stored as a JSON file on the server
 */
public class Configuration {
    /**
     * Email account for sending our verification code from
     */
    @NotNull
    public final String email;

    /**
     * Password to the email account
     */
    @NotNull
    public final String emailPassword;

    /**
     * The keystore file which includes the certificate for this server.
     */
    @NotNull
    public final String keyStoreFile;


    /**
     * The password to the keystore file
     */
    @NotNull
    public final String keyStorePassword;

    /**
     * Default configuration that sets everything to empty. This is just for a default and really should not be used.
     */
    public Configuration() {
        this("", "", "", "");
    }

    public Configuration(@NotNull String email, @NotNull String emailPassword, @NotNull String keyStoreFile, @NotNull String keyStorePassword) {
        this.email = email;
        this.emailPassword = emailPassword;
        this.keyStorePassword = keyStorePassword;
        this.keyStoreFile = keyStoreFile;
    }
}
