package main.java.database.table;

import main.java.database.SQLGenerator;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * An abstract table for storing common information between tables
 */
public abstract class AbstractTable {
    /**
     * An SQL generator that helps create common SQL queries.
     */
    protected final SQLGenerator generator;
    /**
     * Connection to the database
     */
    protected final Connection con;
    /**
     * The table name
     */
    private final String name;

    public AbstractTable(String name, Connection con) {
        this.name = name;
        this.con = con;

        generator = new SQLGenerator(name, con);
    }

    public String getName() {
        return name;
    }

    /**
     * Drops this table from the database
     *
     * @throws SQLException generally this means the table hasn't been created yet
     */
    public void dropTable() throws SQLException {
        runSQL("DROP TABLE " + name);
    }

    /**
     * Creates a table in the database
     *
     * @throws SQLException Generally this means the table is already in the database
     */
    public void createTable() throws SQLException {
        runSQL("CREATE TABLE " + name + " (" + setupTableColumns() + ")");
    }

    /**
     * @return The columns of a table for the Create command. Do not need to include the parens.
     */
    protected abstract String setupTableColumns();

    /**
     * Helper function for running SQL. Use sparingly, since this does not use a prepared statement.
     *
     * @param sql The sql query to run
     */
    private void runSQL(String sql) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();
    }

}
