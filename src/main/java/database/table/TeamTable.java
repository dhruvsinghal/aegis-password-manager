package main.java.database.table;

import main.java.model.AegisTeam;
import main.java.model.NoIDAegisTeam;
import main.java.model.info.TeamInfo;
import main.java.util.Serializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

/**
 * A table representing all the teams
 */
public class TeamTable extends AbstractTable {
    //Default Table Name
    private static final String table_name = "Team";

    //Columns
    private static final String tidCol = "tidCol"; //Integer
    private static final String infoCol = "infoCol"; //Blob

    public TeamTable(Connection con) {
        super(table_name, con);
    }

    public int addTeam(NoIDAegisTeam team) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement p = generator.insertInto(Collections.singletonList(infoCol), true)) {
            p.setBytes(1, Serializer.serialize(team.teamInfo));

            p.executeUpdate();
            ResultSet rs = p.getGeneratedKeys();
            if (rs.next()) {
                long ret = rs.getLong(1);
                if (ret == (int) ret) {
                    return (int) ret; //Yay cast was safe
                }
            }

            throw new SQLException("Unable to add the team");
        }
    }

    public void updateTeam(AegisTeam team) throws SQLException, Serializer.SerializationException {
        deleteTeam(team.id);
        try (PreparedStatement p = generator.insertInto(Arrays.asList(tidCol, infoCol), false)) {
            p.setInt(1, team.id);
            p.setBytes(2, Serializer.serialize(team.teamInfo));
            p.executeUpdate();
        }
    }

    /**
     * Reads in the TeamInfo that corresponds to an eid
     */
    public TeamInfo readTeam(int tid) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement query = generator.getID(Collections.singletonList(tidCol), Collections.singletonList(tid))) {

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                return Serializer.deserialize(rs.getBytes(infoCol));
            } else {
                throw new SQLException("Team does not exist");
            }
        }
    }

    public void deleteTeam(int tid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(tidCol), Collections.singletonList(tid))) {
            query.executeUpdate();
        }
    }

    @Override
    protected String setupTableColumns() {
        return tidCol + " integer     primary key     not null, " +
                infoCol + " blob    not null";
    }
}
