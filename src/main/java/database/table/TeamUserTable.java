package main.java.database.table;

import main.java.model.info.TeamUserInfo;
import main.java.util.Serializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * A table mapping teams to users
 */
public class TeamUserTable extends AbstractTable {
    //TableName
    private static final String table_name = "TeamAndUser";

    //columns
    private static final String uidCol = "uid"; //integer
    private static final String tidCol = "tid"; //integer
    private static final String infoCol = "info"; //blob

    public TeamUserTable(Connection con) {
        super(table_name, con);
    }


    /**
     * Adds a new row into the table
     *
     * @param tid  the teamID
     * @param uid  the userID
     * @param info permission level
     */
    public void addTeamAndUser(int tid, int uid, TeamUserInfo info) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement preparedStatement = generator.insertInto(Arrays.asList(tidCol, uidCol, infoCol), false)) {
            preparedStatement.setInt(1, tid);
            preparedStatement.setInt(2, uid);
            preparedStatement.setBytes(3, Serializer.serialize(info));
            preparedStatement.executeUpdate();
        }
    }

    public HashMap<Integer, TeamUserInfo> getUserPermissions(int uid) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement query = generator.getID(Collections.singletonList(uidCol), Collections.singletonList(uid))) {
            ResultSet rs = query.executeQuery();
            HashMap<Integer, TeamUserInfo> ret = new HashMap<>();
            while (rs.next()) {
                TeamUserInfo info = Serializer.deserialize(rs.getBytes(infoCol));
                ret.put(rs.getInt(tidCol), info);
            }

            return ret;
        }
    }

    public HashMap<Integer, TeamUserInfo> getTeamPermissions(int tid) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement query = generator.getID(Collections.singletonList(tidCol), Collections.singletonList(tid))) {
            ResultSet rs = query.executeQuery();
            HashMap<Integer, TeamUserInfo> ret = new HashMap<>();
            while (rs.next()) {
                TeamUserInfo info = Serializer.deserialize(rs.getBytes(infoCol));
                ret.put(rs.getInt(uidCol), info);
            }

            return ret;
        }
    }

    public void deleteTeam(int tid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(tidCol), Collections.singletonList(tid))) {
            query.executeUpdate();
        }
    }

    public void deleteUser(int uid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(uidCol), Collections.singletonList(uid))) {
            query.executeUpdate();
        }
    }

    @Override
    protected String setupTableColumns() {
        return uidCol + " integer  not null, " +
                tidCol + " integer  not null, " +
                infoCol + " blob     not null, " +
                "unique (" + uidCol + "," + tidCol + ") ON CONFLICT REPLACE";
    }
}
