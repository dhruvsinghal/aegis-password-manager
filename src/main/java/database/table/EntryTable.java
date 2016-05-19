package main.java.database.table;

import main.java.model.AegisEntry;
import main.java.model.NoIDAegisEntry;
import main.java.model.info.EntryInfo;
import main.java.util.Serializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

/**
 * A table representing all the entries
 */
public class EntryTable extends AbstractTable {
    //Default Table Name
    private static final String table_name = "Entry";

    //Columns
    private static final String eidCol = "eid"; //Integer
    private static final String infoCol = "info"; //Blob

    public EntryTable(Connection con) {
        super(table_name, con);
    }

    public int addEntry(NoIDAegisEntry entry) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement p = generator.insertInto(Collections.singletonList(infoCol), true)) {
            p.setObject(1, Serializer.serialize(entry.entryInfo));
            p.executeUpdate();

            ResultSet rs = p.getGeneratedKeys();
            if (rs.next()) {
                long ret = rs.getLong(1);
                if (ret == (int) ret) {
                    return (int) ret; //Yay cast was safe
                }
            }

            throw new SQLException("Unable to add the entry");

        }
    }

    public void updateEntry(AegisEntry entry) throws SQLException, Serializer.SerializationException {
        deleteEntry(entry.id);
        try (PreparedStatement p = generator.insertInto(Arrays.asList(eidCol, infoCol), false)) {
            p.setInt(1, entry.id);
            p.setBytes(2, Serializer.serialize(entry.entryInfo));
            p.executeUpdate();
        }
    }

    /**
     * Reads in the EntryInfo that corresponds to an eid
     */
    public EntryInfo readEntry(int eid) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement query = generator.getID(Collections.singletonList(eidCol), Collections.singletonList(eid))) {

            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                return Serializer.deserialize(rs.getBytes(infoCol));
            } else {
                throw new SQLException("Entry does not exist");
            }
        }
    }

    public void deleteEntry(int eid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(eidCol), Collections.singletonList(eid))) {
            query.executeUpdate();
        }
    }

    @Override
    protected String setupTableColumns() {
        return eidCol + " integer     primary key     not null, " +
                infoCol + " blob    not null";
    }
}
