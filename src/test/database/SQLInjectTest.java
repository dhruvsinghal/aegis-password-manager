package test.database;

import main.java.database.AegisPasswordDatabase;
import main.java.model.AegisEntry;
import main.java.model.AegisTeam;
import main.java.model.AegisUser;
import main.java.model.UserPermissions;
import main.java.model.info.EntryInfo;
import main.java.model.info.TeamInfo;
import main.java.model.info.TeamUserInfo;
import main.java.model.info.UserInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Testing class for SQL injection
 */
@RunWith(Parameterized.class)
public class SQLInjectTest {
    //SQL Inject Attacks
    private static final String[] sqlStrings = {
            "admin' -- a",
            "admin' # a",
            "admin'/* a",
            "' or 1=1-- a",
            "or 1=1# a",
            "' or 1=1/* a",
            "') or '1'='1-- a",
            "') or ('1'='1-- a",
            "ORDER BY 200-- a",
            "# junk",
            "-- hello",
            "admin'--",
            "10;DROP members --",
            "0x50 + 0x45",
            "username + '-' + password",
            "login || '-' || password",
            "CHAR(75)+CHAR(76)+CHAR(77)",
            "CHR(75)||CHR(76)||CHR(77)",

            //Version specific attack
            "/*!32302 1/0, */",
    };

    @Parameter
    public String sql;

    //ID for overall
    private final int testID = 1;

    private final int entryTeam = 1; //Entry is part of team 1

    //User info
    private HashMap<Integer, TeamUserInfo> userAccess; //<team, access>
    //Team info
    private HashMap<Integer, TeamUserInfo> teamUsers; //<user, access>

    private List<Integer> teamEntries;
    //Useful stuff that is in setUp
    private AegisPasswordDatabase db;
    private AegisUser testUser;
    private AegisEntry testEntry;
    private AegisTeam testTeam;

    @Parameters(name = "{0}")
    public static Collection getSQLInjectionTests() {
        ArrayList<String> ret = new ArrayList<>();
        Collections.addAll(ret, sqlStrings);
        return ret;
    }

    @Before
    public void setUp() throws Exception {
        db = new AegisPasswordDatabase("test.db");
        db.reset();

        userAccess = new HashMap<>();
        userAccess.put(1, new TeamUserInfo(UserPermissions.ADMIN, sql)); //user 1 has admin access to team 1
        userAccess.put(2, new TeamUserInfo(UserPermissions.WRITE, sql)); //user 1 has write access to team 2
        userAccess.put(3, new TeamUserInfo(UserPermissions.READ, sql)); //user 1 has write access to team 3


        teamUsers = new HashMap<>();
        teamUsers.put(1, new TeamUserInfo(UserPermissions.ADMIN, sql)); //user has admin access to team 1
        teamUsers.put(2, new TeamUserInfo(UserPermissions.WRITE, sql)); //user 2 has write access to team 1
        teamUsers.put(3, new TeamUserInfo(UserPermissions.READ, sql)); //user 3 has read access to team 1

        //Entries 1 and 2 are part of team 1
        teamEntries = new ArrayList<>();
        teamEntries.add(1);
        teamEntries.add(2);
        testUser = new AegisUser(new UserInfo(sql, sql, sql, sql, sql, sql, sql, sql, sql, sql), sql, userAccess, testID);
        testEntry = new AegisEntry(new EntryInfo(sql, sql, sql, sql), entryTeam, testID);
        testTeam = new AegisTeam(new TeamInfo(sql), teamEntries, teamUsers, testID);
    }

    @After
    public void tearDown() throws Exception {
        db.reset(); //comment this out if you want to inspect the database after a run
        db.close();

        db = null;
        testUser = null;
        testEntry = null;
        testTeam = null;
        System.gc();
    }

    /**
     * Tests operate under the assumption that if we produce the same exact object back we are injection proof
     * because otherwise we wouldn't have stored some of the data
     */

    @Test
    public void testUpdateAegisUser() throws Exception {
        db.createUser(testUser);
        AegisUser au = db.readUser(testID).get();
        assertEquals(testUser, au);


        userAccess.put(100, new TeamUserInfo(UserPermissions.ADMIN, sql)); //Trivial change, mostly want to check update is SQL proof
        testUser = new AegisUser(new UserInfo(sql, sql, sql, sql, sql, sql, sql, sql, sql, sql), sql, userAccess, testID);

        au = db.readUser(testID).get();
        assertNotEquals(testUser, au);

        db.updateUser(testUser);
        au = db.readUser(testID).get();
        assertEquals(testUser, au);
    }

    @Test
    public void testUpdateAegisEntry() throws Exception {
        db.createEntry(testEntry);
        AegisEntry ae = db.readEntry(testID).get();
        assertEquals(testEntry, ae);

        //Trivial change, mostly want to check update is SQL proof
        testEntry = new AegisEntry(new EntryInfo(sql, sql, sql, sql), entryTeam + 1, testID);

        ae = db.readEntry(testID).get();
        assertNotEquals(testTeam, ae);

        db.updateEntry(testEntry);
        ae = db.readEntry(testID).get();
        assertEquals(testEntry, ae);
    }

    @Test
    public void testUpdateAegisTeam() throws Exception {
        db.createTeam(testTeam);
        AegisTeam at = db.readTeam(testID).get();
        assertEquals(testTeam, at);

        //Trivial change, mostly want to check update is SQL proof
        teamEntries.add(10);
        testTeam = new AegisTeam(new TeamInfo(sql), teamEntries, teamUsers, testID);

        at = db.readTeam(testID).get();
        assertNotEquals(testTeam, at);

        db.updateTeam(testTeam);
        at = db.readTeam(testID).get();
        assertEquals(testTeam, at);
    }
}
