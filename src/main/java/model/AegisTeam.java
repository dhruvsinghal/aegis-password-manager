package main.java.model;

import main.java.model.info.TeamInfo;
import main.java.model.info.TeamUserInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * This class contains all the information stored about a specific team
 */
public class AegisTeam extends NoIDAegisTeam {
    /**
     * The id that corresponds to this team
     */
    public final int id;

    public AegisTeam(@NotNull TeamInfo teamInfo, @NotNull List<Integer> entries, @NotNull Map<Integer, TeamUserInfo> users, int id) {
        super(teamInfo, entries, users);
        this.id = id;
    }

    public AegisTeam withUsers(@NotNull Map<Integer, TeamUserInfo> users) {
        return new AegisTeam(teamInfo, entries, users, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AegisTeam aegisTeam = (AegisTeam) o;

        return id == aegisTeam.id;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        return result;
    }
}
