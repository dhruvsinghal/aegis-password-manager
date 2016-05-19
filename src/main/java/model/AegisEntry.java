package main.java.model;

import main.java.model.info.EntryInfo;
import org.jetbrains.annotations.NotNull;

/**
 * This class contains all the information stored about a specific entry
 */
public class AegisEntry extends NoIDAegisEntry {
    /**
     * The ID that corresponds to this entry
     */
    public final int id;

    public AegisEntry(@NotNull EntryInfo entryInfo, int team, int id) {
        super(entryInfo, team);
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AegisEntry that = (AegisEntry) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id;
        return result;
    }
}
