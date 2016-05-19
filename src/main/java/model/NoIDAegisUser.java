package main.java.model;

import main.java.model.info.TeamUserInfo;
import main.java.model.info.UserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AegisUser with no ID field. Used to create an AegisUser in the database
 */
public class NoIDAegisUser {
    @NotNull
    public final UserInfo userInfo;

    /**
     * The email of this user
     */
    @NotNull
    public final String email;

    /**
     * HashMap from TeamID to the permission a user has in that team.
     * This map is immutable.
     */
    @NotNull
    public final Map<Integer, TeamUserInfo> teams;

    /**
     * @param teams This should be the map from TeamID to Permission this user has on the team.
     */
    public NoIDAegisUser(@NotNull UserInfo userInfo, @NotNull String email, @NotNull Map<Integer, TeamUserInfo> teams) {
        this.userInfo = userInfo;
        this.email = email;

        //Make an unmodifiable deep copy so it's immutable.
        this.teams = Collections.unmodifiableMap(new HashMap<>(teams));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoIDAegisUser that = (NoIDAegisUser) o;

        if (!userInfo.equals(that.userInfo)) return false;
        if (!email.equals(that.email)) return false;
        return teams.equals(that.teams);

    }

    @Override
    public int hashCode() {
        int result = userInfo.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + teams.hashCode();
        return result;
    }
}
