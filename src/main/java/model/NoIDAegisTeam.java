package main.java.model;

import main.java.model.info.TeamInfo;
import main.java.model.info.TeamUserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * AegisTeam with no ID field. Used to create an AegisTeam in the database
 */
public class NoIDAegisTeam {
    @NotNull
    public final TeamInfo teamInfo;

    /**
     * An list of all the entries mapped to this team (entryID).
     * This list is immutable.
     */
    @NotNull
    public final List<Integer> entries;

    /**
     * HashMap from userID to the permission a user has in this team.
     * This map is immutable.
     */
    @NotNull
    public final Map<Integer, TeamUserInfo> users;

    public NoIDAegisTeam(@NotNull TeamInfo teamInfo, @NotNull List<Integer> entries, @NotNull Map<Integer, TeamUserInfo> users) {
        this.teamInfo = teamInfo;

        //make unmodifiable deep copies and don't store references so it's immutable.
        this.entries = Collections.unmodifiableList(new ArrayList<>(entries));
        this.users = Collections.unmodifiableMap(new HashMap<>(users));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoIDAegisTeam that = (NoIDAegisTeam) o;

        if (!teamInfo.equals(that.teamInfo)) return false;
        if (!entries.equals(that.entries)) return false;
        return users.equals(that.users);

    }

    @Override
    public int hashCode() {
        int result = teamInfo.hashCode();
        result = 31 * result + entries.hashCode();
        result = 31 * result + users.hashCode();
        return result;
    }
}
