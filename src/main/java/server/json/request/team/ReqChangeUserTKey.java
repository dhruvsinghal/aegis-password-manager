package main.java.server.json.request.team;

import org.jetbrains.annotations.NotNull;

/**
 * JSON for changing the team/user key
 */
public class ReqChangeUserTKey {
    /**
     * uid of user to add to team
     */
    public final int uid;

    /**
     * tid of team to add the user to
     */
    public final int tid;

    /**
     * the team key, encrypted by the user's public key
     */
    @NotNull
    public final String teamKey;

    public ReqChangeUserTKey(int uid, int tid, @NotNull String teamKey) {
        this.uid = uid;
        this.tid = tid;
        this.teamKey = teamKey;
    }
}
