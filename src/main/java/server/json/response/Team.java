package main.java.server.json.response;

import main.java.model.UserPermissions;
import org.jetbrains.annotations.NotNull;

/**
 * A JSON containing the information relevant to a specific user (the active user) about a specific team.
 */
public class Team {
    /**
     * The id that corresponds to this team
     */
    public final int id;

    /**
     * The teamname
     */
    @NotNull
    public final String teamname;

    /**
     * Permission level the user has in this team
     */
    @NotNull
    public final UserPermissions permissions;

    /**
     * The AES key for this team, encrypted by this users public key (so only this user can
     * decrypt this key using their private key). Decrypting this gets you the team password.
     */
    @NotNull
    public final String teamKey;

    public Team(int id, @NotNull String teamname, @NotNull UserPermissions permissions, @NotNull String teamKey) {
        this.id = id;
        this.teamname = teamname;
        this.permissions = permissions;
        this.teamKey = teamKey;
    }
}
