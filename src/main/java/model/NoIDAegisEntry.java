package main.java.model;

import main.java.model.info.EntryInfo;
import org.jetbrains.annotations.NotNull;

/**
 * AegisEntry with no ID field. Used to create an AegisEntry in the database
 */
public class NoIDAegisEntry {
    @NotNull
    public final EntryInfo entryInfo;

    /**
     * The team this entry is mapped to (TeamID).
     */
    public final int team;

    public NoIDAegisEntry(@NotNull EntryInfo entryInfo, int team) {
        this.entryInfo = entryInfo;
        this.team = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoIDAegisEntry that = (NoIDAegisEntry) o;

        if (team != that.team) return false;
        return entryInfo.equals(that.entryInfo);

    }

    @Override
    public int hashCode() {
        int result = entryInfo.hashCode();
        result = 31 * result + team;
        return result;
    }
}
