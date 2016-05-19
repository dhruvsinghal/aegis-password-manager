package main.java.model.info;

import main.java.model.UserPermissions;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * All the extra information associated with a team and user pair
 */
public class TeamUserInfo implements Serializable {
    /**
     * Permission level
     */
    @NotNull
    public final UserPermissions permissions;

    /**
     * The AES key for this team, encrypted by this users public key (so only this user can
     * decrypt this key using their private key). Decrypting this gets you the team password.
     */
    @NotNull
    public final String teamKey;

    public TeamUserInfo(@NotNull UserPermissions permissions, @NotNull String teamKey) {
        this.permissions = permissions;
        this.teamKey = teamKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TeamUserInfo that = (TeamUserInfo) o;

        if (permissions != that.permissions) return false;
        return teamKey.equals(that.teamKey);

    }

    @Override
    public int hashCode() {
        int result = permissions.hashCode();
        result = 31 * result + teamKey.hashCode();
        return result;
    }
}
