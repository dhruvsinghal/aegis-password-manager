package main.java.server.json.request.team;

import main.java.model.UserPermissions;
import org.jetbrains.annotations.NotNull;

/**
 * JSON for changing the team/user permissions
 */
public class ReqChangeUserPermissions {
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
    public final @NotNull UserPermissions permissions;

    public ReqChangeUserPermissions(int uid, int tid, @NotNull UserPermissions permissions) {
        this.uid = uid;
        this.tid = tid;
        this.permissions = permissions;
    }
}
