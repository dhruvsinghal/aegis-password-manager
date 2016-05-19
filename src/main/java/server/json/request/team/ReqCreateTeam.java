package main.java.server.json.request.team;

import org.jetbrains.annotations.NotNull;

/**
 * JSON for creating a team
 */
public class ReqCreateTeam {
    /**
     * the team key, encrypted by the your own public key
     */
    @NotNull
    public final String teamKey;

    /**
     * A name for your team
     */
    @NotNull
    public final String teamName;

    public ReqCreateTeam(@NotNull String teamKey, @NotNull String teamName) {
        this.teamKey = teamKey;
        this.teamName = teamName;
    }
}
