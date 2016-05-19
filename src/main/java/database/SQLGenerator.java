package main.java.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for generating prepared statements.
 * <p>
 * Note that all arguments to every method assumes the argument is not null.
 */
public class SQLGenerator {
    //prepared statement names. Public since it may be useful to use them in other places
    public final String insert_into;
    public final String select_from_where;
    public final String delete_from_where;

    /**
     * The connection used to generate the prepared statements
     */
    private final Connection con;

    public SQLGenerator(String tableName, Connection con) {
        this.con = con;

        insert_into = "INSERT INTO " + tableName + " (";
        select_from_where = "SELECT * FROM " + tableName + " WHERE ";
        delete_from_where = "DELETE FROM " + tableName + " WHERE ";
    }


    /**
     * Helper function for generating SQL commands that simplifies a common template
     *
     * @param initalString The string to start with
     * @param list         List of elements to add in. Requires the size of the list is greater than 0
     * @param afterNonLast String to place after every non-last element
     * @param afterLast    String to place after the last element
     * @return The string generated
     */
    private String forAllButLast(String initalString, List<String> list, String afterNonLast, String afterLast) {
        StringBuilder ret = new StringBuilder(initalString);

        //non last elements
        for (int i = 0; i < list.size() - 1; i++) {
            ret.append(list.get(i));
            ret.append(afterNonLast);
        }

        //last element
        ret.append(list.get(list.size() - 1));
        ret.append(afterLast);

        return ret.toString();
    }

    /**
     * Create an insert into query. Requires the user to actually set the prepared elements since
     * we do not know their types.
     *
     * @param colNames the name of the columns.
     * @param genKey   if to set the statement to RETURN_GENERATED_KEYS.
     * @return a prepared statement
     */
    public PreparedStatement insertInto(List<String> colNames, boolean genKey) throws SQLException {
        if (colNames.size() == 0) {
            throw new SQLException("Invalid insert into");
        }

        String temp = forAllButLast(insert_into, colNames, ",", ") VALUES (");

        //Make '?' array of the same size as the colNames
        List<String> params = new ArrayList<>();
        colNames.forEach(x -> params.add("?"));

        //Insert in the optional params part
        String sql = forAllButLast(temp, params, ",", ")");

        PreparedStatement ret;
        if (genKey) {
            ret = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } else {
            ret = con.prepareStatement(sql);
        }

        return ret;
    }

    /**
     * Creates the following Query: DELETE FROM table WHERE colName[0] = id[0] and colName[1] = id[1]...
     *
     * @param colNames The names of the id columns
     * @param id       the ID to delete
     * @return The prepared statement for the table to run
     * @throws SQLException if there is an issue throws and SQL exception
     */
    public PreparedStatement deleteID(List<String> colNames, List<Integer> id) throws SQLException {
        if (colNames.size() == 0 || id.size() == 0 || colNames.size() != id.size()) {
            throw new SQLException("Invalid deleteID request");
        }

        String query = forAllButLast(delete_from_where, colNames, "=? and ", "=?");
        PreparedStatement ret = con.prepareStatement(query);
        for (int i = 0; i < id.size(); i++) {
            ret.setInt(i + 1, id.get(i));
        }

        return ret;
    }

    /**
     * Creates the following Query: Select * From table Where colName[0] = id[0] and colName[1] = id[1]...
     *
     * @param colNames The names of the id columns
     * @param id       the ID to select for
     * @return The prepared statement for the table to run
     * @throws SQLException if there is an issue throws and SQL exception
     */
    public PreparedStatement getID(List<String> colNames, List<Integer> id) throws SQLException {
        if (colNames.size() == 0 || id.size() == 0 || colNames.size() != id.size()) {
            throw new SQLException("Invalid getID request");
        }

        String query = forAllButLast(select_from_where, colNames, "=? and ", "=?");

        PreparedStatement ret = con.prepareStatement(query);
        for (int i = 0; i < id.size(); i++) {
            ret.setInt(i + 1, id.get(i));
        }
        return ret;
    }
}
