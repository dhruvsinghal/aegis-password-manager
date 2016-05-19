package main.java.database.table;

import main.java.model.AegisUser;
import main.java.model.NoIDAegisUser;
import main.java.model.info.UserInfo;
import main.java.util.Serializer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

/**
 * A table representing all the users
 */
public class UserTable extends AbstractTable {
    //Default Table Name
    private static final String table_name = "User";

    //Columns
    private static final String uidCol = "uid"; //Integer
    private static final String emailCol = "email"; //Text
    private static final String infoCol = "info"; //Blob

    public UserTable(Connection con) {
        super(table_name, con);
    }

    public int addUser(NoIDAegisUser user) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement p = generator.insertInto(Arrays.asList(emailCol, infoCol), true)) {
            p.setString(1, user.email);
            p.setBytes(2, Serializer.serialize(user.userInfo));

            p.executeUpdate();
            ResultSet rs = p.getGeneratedKeys();
            if (rs.next()) {
                long ret = rs.getLong(1);
                if (ret == (int) ret) {
                    return (int) ret; //Yay cast was safe
                }
            }

            throw new SQLException("Unable to add the user");
        }
    }

    public void updateUser(AegisUser user) throws SQLException, Serializer.SerializationException {
        deleteUser(user.id);
        try (PreparedStatement p = generator.insertInto(Arrays.asList(uidCol, emailCol, infoCol), false)) {
            p.setInt(1, user.id);
            p.setString(2, user.email);
            p.setBytes(3, Serializer.serialize(user.userInfo));
            p.executeUpdate();
        }
    }

    /**
     * Reads in the relevant components of an AegisUser from this table. In otherwords, reads in the
     * uidCol, emailCol, and aegis user info. Sets the other fields to trivial values that will have to be set later.
     */
    public AegisUser readUser(int uid) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement query = generator.getID(Collections.singletonList(uidCol), Collections.singletonList(uid))) {
            ResultSet rs = query.executeQuery();
            if (rs.next()) {
                String email = rs.getString(emailCol);
                UserInfo info = Serializer.deserialize(rs.getBytes(infoCol));
                return new AegisUser(info, email, new HashMap<>(), uid);
            } else {
                throw new SQLException("User does not exist");
            }
        }
    }

    /**
     * Gets the UID of the user that corresponds to this email address.
     */
    public int getID(String email) throws SQLException, Serializer.SerializationException {
        try (PreparedStatement query = con.prepareStatement(generator.select_from_where + emailCol + "=?")) {
            query.setString(1, email);
            ResultSet rs = query.executeQuery();

            if (rs.next()) {
                return rs.getInt(uidCol);
            } else {
                throw new SQLException("User does not exist");
            }
        }
    }

    public void deleteUser(int uid) throws SQLException {
        try (PreparedStatement query = generator.deleteID(Collections.singletonList(uidCol), Collections.singletonList(uid))) {
            query.executeUpdate();
        }
    }

    @Override
    protected String setupTableColumns() {
        return uidCol + " integer     primary key     not null, " +
                emailCol + " text  unique  not null, " +
                infoCol + " blob   not null";
    }
}
