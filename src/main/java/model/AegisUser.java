package main.java.model;

import main.java.model.info.TeamUserInfo;
import main.java.model.info.UserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * This class contains all the information stored about a specific user
 */
public class AegisUser extends NoIDAegisUser {
    /**
     * The id that corresponds to this user
     */
    public final int id;

    public AegisUser(@NotNull UserInfo userInfo, @NotNull String email, @NotNull Map<Integer, TeamUserInfo> teams, int id) {
        super(userInfo, email, teams);
        this.id = id;
    }

    public AegisUser withUserInfo(@NotNull UserInfo userInfo) {
        return new AegisUser(userInfo, email, teams, id);
    }

    public AegisUser withEmail(@NotNull String email) {
        return new AegisUser(userInfo, email, teams, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AegisUser aegisUser = (AegisUser) o;

        return id == aegisUser.id;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        return result;
    }
}
