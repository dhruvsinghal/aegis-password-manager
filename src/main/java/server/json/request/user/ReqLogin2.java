package main.java.server.json.request.user;

import org.jetbrains.annotations.NotNull;

/**
 * JSON to pass in for login 1
 */
public class ReqLogin2 {
    /**
     * Email of the user wishing to log in.
     */
    @NotNull
    public final String email;

    /**
     * The hashed master password of the user.
     */
    @NotNull
    public final String hashedMasterPassword;

    /**
     * The verification code for two factor authentication
     */
    @NotNull
    public final String code;

    public ReqLogin2(@NotNull String email, @NotNull String hashedMasterPassword, @NotNull String code) {
        this.email = email;
        this.hashedMasterPassword = hashedMasterPassword;
        this.code = code;
    }
}
