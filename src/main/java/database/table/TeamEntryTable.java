package main.java.database.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A table mapping teams to entries
 */
public class TeamEntryTable extends AbstractTable {
    //TableName
    private static final String table_name = "TeamAndEntry";

    //columns
    private static final String eidCol = "eid"; //integer
    private static final String tidCol = "tid"; //integer

    public TeamEntryTable(Connection con) {
        super(table_name, con);
    }

    /**
     * Adds a new entry into the table
     *
     * @param tid the teamID
     * @param eid the entryID
     */
    public void addTeamAndEntry(int tid, int eid) throws SQLException {
        try (PreparedStatement preparedStatement = generator.insertInto(Arrays.asList(tidCol, eidCol), false)) {
            preparedStatement.setInt(1, tid);
            preparedStatement.setInt(2, eid);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Gets the team that corresponds to the entryID
     *
     * @param eid the entryID
     */
    public int getTeam(int eid) throws SQLException {
        try (PreparedStatement query = generator.getID(Collections.singletonList(eidCol), Collections.singletonList(eid))) {
            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                return rs.getInt(tidCol);
            }

            throw new SQLException("Unable to find entry");
        }
    }

    /**
     * Get the entries that correspond to a team.
     *
     * @param tid the teamID
     * @return A list of teams
     */
    public List<Integer> getEntries(int tid) throws SQLException {
        List<Integer> ret = new ArrayList<>();
        try (PreparedStatement query = generator.getID(Collections.singletonList(tidCol), Collections.singletonList(tid))) {
            ResultSet rs = query.executeQuery();
            while (rs.next()) {
                ret.add(rs.getInt(eidCol));
            }
        }

        return ret;
    }

    public void deleteEntry(int eid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(eidCol), Collections.singletonList(eid))) {
            query.executeUpdate();
        }
    }

    public void deleteTeam(int tid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(tidCol), Collections.singletonList(tid))) {
            query.executeUpdate();
        }
    }

    @Override
    protected String setupTableColumns() {
        return eidCol + " integer unique ON CONFLICT REPLACE not null, " +
                tidCol + " integer  not null";
    }
}
