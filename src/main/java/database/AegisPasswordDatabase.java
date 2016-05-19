package main.java.database;

import main.java.database.table.*;
import main.java.model.*;
import main.java.model.info.EntryInfo;
import main.java.model.info.TeamInfo;
import main.java.model.info.TeamUserInfo;
import main.java.util.Serializer;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * The primary database for storing user, table, and entry info.
 * <p>
 * Note that internally there are a lot of helper methods. In general, these methods are for
 * using as a lambda to pass onto runAsTransaction (private method in AegisPasswordDatabase)
 */
public class AegisPasswordDatabase implements PasswordDatabase {
    //The con to the database. All tables should use this con too
    private final Connection con;

    //Tables
    private final UserTable userTable;
    private final TeamTable teamTable;
    private final EntryTable entryTable;
    private final TeamEntryTable teamEntryTable;
    private final TeamUserTable teamUserTable;

    /**
     * List of all the tables for convenience.
     */
    private final List<AbstractTable> tables;

    /**
     * @param file The file name
     * @throws SQLException A
     */
    public AegisPasswordDatabase(@NotNull String file) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Getting this error probably means you do not have JDBC setup");
            e.printStackTrace();
        }

        con = DriverManager.getConnection("jdbc:sqlite:" + file);

        //setup tables
        userTable = new UserTable(con);
        teamTable = new TeamTable(con);
        entryTable = new EntryTable(con);
        teamEntryTable = new TeamEntryTable(con);
        teamUserTable = new TeamUserTable(con);

        //Add tables to list
        tables = new ArrayList<>();
        tables.add(userTable);
        tables.add(teamTable);
        tables.add(entryTable);
        tables.add(teamEntryTable);
        tables.add(teamUserTable);
    }

    /**
     * Closes the connection to the database.
     *
     * @throws SQLException
     */
    public void close() throws SQLException {
        con.close();
    }

    @Override
    public void reset() {
        for (AbstractTable t : tables) {
            try {
                t.dropTable();
            } catch (SQLException e) { //Should not be an issue. Table probably doesn't exist
                //e.printStackTrace();
            }
        }

        for (AbstractTable t : tables) {
            try {
                t.createTable();
            } catch (SQLException e) {
                //Indicates there is a potential issue initializing the table
                System.err.println("Was unable to initialize a table correctly: " + t.getName());
                //e.printStackTrace();
            }
        }
    }

    //create methods
    private int createUserHelper(NoIDAegisUser user) throws SQLException, Serializer.SerializationException {
        int uid = userTable.addUser(user);
        for (Map.Entry<Integer, TeamUserInfo> e : user.teams.entrySet()) {
            teamUserTable.addTeamAndUser(e.getKey(), uid, e.getValue());
        }
        return uid;
    }

    private int createEntryHelper(NoIDAegisEntry entry) throws SQLException, Serializer.SerializationException {
        int eid = entryTable.addEntry(entry);
        teamEntryTable.addTeamAndEntry(entry.team, eid);
        return eid;
    }

    private int createTeamHelper(NoIDAegisTeam team) throws SQLException, Serializer.SerializationException {
        int tid = teamTable.addTeam(team);
        for (Map.Entry<Integer, TeamUserInfo> e : team.users.entrySet()) { //users
            teamUserTable.addTeamAndUser(tid, e.getKey(), e.getValue());
        }
        for (int eid : team.entries) { //entries
            teamEntryTable.addTeamAndEntry(tid, eid);
        }
        return tid;
    }

    @Override
    public @NotNull Optional<Integer> createUser(@NotNull NoIDAegisUser user) {
        return runAsTransaction(() -> createUserHelper(user));
    }

    @Override
    public @NotNull Optional<Integer> createEntry(@NotNull NoIDAegisEntry entry) {
        return runAsTransaction(() -> createEntryHelper(entry));
    }

    @Override
    public @NotNull Optional<Integer> createTeam(@NotNull NoIDAegisTeam team) {
        return runAsTransaction(() -> createTeamHelper(team));
    }


    //read methods
    private AegisUser readUserHelper(int uid) throws SQLException, Serializer.SerializationException {
        AegisUser temp = userTable.readUser(uid);
        HashMap<Integer, TeamUserInfo> teams = teamUserTable.getUserPermissions(uid);
        return new AegisUser(temp.userInfo, temp.email, teams, temp.id);
    }

    private AegisUser readUserHelper(String email) throws SQLException, Serializer.SerializationException {
        int uid = userTable.getID(email);
        return readUserHelper(uid);
    }

    private AegisEntry readEntryHelper(int eid) throws SQLException, Serializer.SerializationException {
        EntryInfo info = entryTable.readEntry(eid);
        int team = teamEntryTable.getTeam(eid);
        return new AegisEntry(info, team, eid);
    }

    private AegisTeam readTeamHelper(int tid) throws SQLException, Serializer.SerializationException {
        TeamInfo info = teamTable.readTeam(tid);
        List<Integer> entries = teamEntryTable.getEntries(tid);
        HashMap<Integer, TeamUserInfo> users = teamUserTable.getTeamPermissions(tid);
        return new AegisTeam(info, entries, users, tid);
    }

    @Override
    public @NotNull Optional<AegisUser> readUser(@NotNull String email) {
        return runAsTransaction(() -> readUserHelper(email));
    }

    @Override
    public @NotNull Optional<AegisUser> readUser(int id) {
        return runAsTransaction(() -> readUserHelper(id));
    }

    @Override
    public @NotNull Optional<AegisEntry> readEntry(int id) {
        return runAsTransaction(() -> readEntryHelper(id));
    }

    @Override
    public @NotNull Optional<AegisTeam> readTeam(int id) {
        return runAsTransaction(() -> readTeamHelper(id));
    }


    //update methods
    private Success updateUserHelper(AegisUser user) throws SQLException, Serializer.SerializationException {
        userTable.readUser(user.id); //Test if the user exists. Not sure if necessary

        userTable.updateUser(user);
        teamUserTable.deleteUser(user.id);
        for (Map.Entry<Integer, TeamUserInfo> e : user.teams.entrySet()) {
            teamUserTable.addTeamAndUser(e.getKey(), user.id, e.getValue());
        }
        return Success.get();
    }

    private Success updateTeamHelper(AegisTeam team) throws SQLException, Serializer.SerializationException {
        teamTable.readTeam(team.id); //Test if the team exists. Not sure if necessary

        teamTable.updateTeam(team);
        teamUserTable.deleteTeam(team.id);
        teamEntryTable.deleteTeam(team.id);
        for (Map.Entry<Integer, TeamUserInfo> e : team.users.entrySet()) { //users
            teamUserTable.addTeamAndUser(team.id, e.getKey(), e.getValue());
        }
        for (int eid : team.entries) { //entries
            teamEntryTable.addTeamAndEntry(team.id, eid);
        }
        return Success.get();
    }

    private Success updateEntryHelper(AegisEntry entry) throws SQLException, Serializer.SerializationException {
        entryTable.readEntry(entry.id); //Test if the entry exists. Not sure if necessary

        entryTable.updateEntry(entry);
        teamEntryTable.addTeamAndEntry(entry.team, entry.id);
        return Success.get();
    }

    @Override
    public boolean updateUser(@NotNull AegisUser user) {
        return runAsTransaction(() -> updateUserHelper(user)).isPresent();
    }

    @Override
    public boolean updateEntry(@NotNull AegisEntry entry) {
        return runAsTransaction(() -> updateEntryHelper(entry)).isPresent();
    }

    @Override
    public boolean updateTeam(@NotNull AegisTeam team) {
        return runAsTransaction(() -> updateTeamHelper(team)).isPresent();
    }


    //delete methods
    private Success deleteUserHelper(int uid) throws SQLException {
        userTable.deleteUser(uid);
        teamUserTable.deleteUser(uid);
        return Success.get();
    }

    private Success deleteTeamHelper(int tid) throws SQLException {
        teamTable.deleteTeam(tid);
        teamUserTable.deleteTeam(tid);
        teamEntryTable.deleteTeam(tid);
        return Success.get();
    }

    private Success deleteEntryHelper(int eid) throws SQLException {
        entryTable.deleteEntry(eid);
        teamEntryTable.deleteEntry(eid);
        return Success.get();
    }

    @Override
    public boolean deleteUser(int id) {
        return runAsTransaction(() -> deleteUserHelper(id)).isPresent();
    }

    @Override
    public boolean deleteEntry(int id) {
        return runAsTransaction(() -> deleteEntryHelper(id)).isPresent();
    }

    @Override
    public boolean deleteTeam(int id) {
        return runAsTransaction(() -> deleteTeamHelper(id)).isPresent();
    }


    /**
     * Tries to run the given SQLfunction as a transaction. If unsuccessful, rolls back the transaction and
     * returns an empty optional.
     * <p>
     * Usage looks like:
     * runAsTransaction(() -> updateTeam(team));
     *
     * @param fun The function to run. Please use SQL statements normally in this function.
     * @param <R> The return type of the optional
     * @return An optional that will contain the return value if the operation was successful.
     */
    private synchronized <R> Optional<R> runAsTransaction(CheckedSQLFunction<R> fun) {
        Optional<R> ret;

        try {
            con.setAutoCommit(false);
            ret = Optional.ofNullable(fun.call());
            con.commit();
        } catch (SQLException | Serializer.SerializationException e) {
            //e.printStackTrace(); //Comment in to see issues
            System.err.println("Error occurred. Transaction is being rolled back");
            try {
                con.rollback();
            } catch (SQLException e1) {
                System.err.println("Issue rolling back the transaction. This is a serious issue.");
                e1.printStackTrace();
            }

            ret = Optional.empty();
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Issue setting auto commit back to true. This is a serious issue.");
                e.printStackTrace();
            }
        }

        return ret;
    }

    /**
     * Interface for SQL functions. This is useful for the runAsTransaction method.
     *
     * @param <R> The return type.
     */
    @FunctionalInterface
    private interface CheckedSQLFunction<R> {
        R call() throws SQLException, Serializer.SerializationException;
    }

    /**
     * Singleton object for successes.
     */
    private static class Success {
        private static final Success instance = new Success();

        public static Success get() {
            return instance;
        }
    }
}
