package main.java.model.info;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * All the extra information associated with a team
 */
public class TeamInfo implements Serializable {


    /**
     * The name of this team
     */
    @NotNull
    public final String teamName;

    public TeamInfo(@NotNull String teamName) {
        this.teamName = teamName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamInfo teamInfo = (TeamInfo) o;

        return teamName.equals(teamInfo.teamName);

    }

    @Override
    public int hashCode() {
        return teamName.hashCode();
    }
}
