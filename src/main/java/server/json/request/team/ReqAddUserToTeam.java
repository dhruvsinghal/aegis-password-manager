package main.java.server.json.request.team;

import main.java.model.UserPermissions;
import org.jetbrains.annotations.NotNull;

/**
 * JSON for adding a user to the team
 */
public class ReqAddUserToTeam {
    /**
     * uid of user to add to team
     */
    public final int uid;

    /**
     * tid of team to add the user to
     */
    public final int tid;

    /**
     * privilage the user will have
     */
    @NotNull
    public final UserPermissions permissions;

    /**
     * the team key, encrypted by the user's public key
     */
    @NotNull
    public final String teamKey;

    public ReqAddUserToTeam(int uid, int tid, @NotNull UserPermissions permissions, @NotNull String teamKey) {
        this.uid = uid;
        this.tid = tid;
        this.permissions = permissions;
        this.teamKey = teamKey;
    }
}
