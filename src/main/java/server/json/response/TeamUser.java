package main.java.server.json.response;

import main.java.model.AegisUser;
import main.java.model.UserPermissions;
import org.jetbrains.annotations.NotNull;

/**
 * Information about a user of a team
 */
public class TeamUser extends PartialUser {
    /**
     * The user's permission level in the team.
     */
    @NotNull
    public final UserPermissions permission;

    /**
     * The AES key for this team, encrypted by this users public key (so only this user can
     * decrypt this key using their private key). Decrypting this gets you the team password.
     */
    @NotNull
    public final String teamKey;

    public TeamUser(AegisUser user, @NotNull UserPermissions permission, @NotNull String teamKey) {
        super(user);
        this.permission = permission;
        this.teamKey = teamKey;
    }
}
