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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Testing class for the password database
 */
public class AegisPasswordDatabaseTest {
    private final int testID = 1;

    //User info
    private final String userEmail = "mrc26@cornell.edu";
    private final String userFirstName = "Michael";
    private final String userLastName = "Clarkson";
    public final String hashedMasterPassword = "password";
    public final String masterPasswordSalt = "salt1";
    public final String encKeyDerivationSalt = "saltEnc";
    public final String macKeyDerivationSalt = "saltMAC";
    public final String publicKey = "public";
    public final String publicKeyMAC = "publicMAC";
    public final String keyIV = "iv";
    public final String privateKey = "private";

    //Entry info
    private final String entryTitle = "Facebook";
    private final String entryUsername = "mrc26@cornell.edu";
    private final String entryPassword = "password";
    private final String iv = "iv";
    private final int entryTeam = 1; //Entry is part of team 1

    //Team info
    private final String teamName = "CS5430";
    private final String teamPassword = "team pass";
    private HashMap<Integer, TeamUserInfo> userAccess; //<team, access>
    private HashMap<Integer, TeamUserInfo> teamUsers; //<user, access>
    private List<Integer> teamEntries;

    //Useful stuff that is in setUp
    private AegisPasswordDatabase db;
    private AegisUser testUser;
    private AegisEntry testEntry;
    private AegisTeam testTeam;


    @Before
    public void setUp() throws Exception {
        db = new AegisPasswordDatabase("test.db");
        db.reset();

        userAccess = new HashMap<>();
        userAccess.put(1, new TeamUserInfo(UserPermissions.ADMIN, teamPassword)); //user 1 has admin access to team 1
        userAccess.put(2, new TeamUserInfo(UserPermissions.WRITE, teamPassword)); //user 1 has write access to team 2
        userAccess.put(3, new TeamUserInfo(UserPermissions.READ, teamPassword)); //user 1 has write access to team 3


        teamUsers = new HashMap<>();
        teamUsers.put(1, new TeamUserInfo(UserPermissions.ADMIN, teamPassword)); //user has admin access to team 1
        teamUsers.put(2, new TeamUserInfo(UserPermissions.WRITE, teamPassword)); //user 2 has write access to team 1
        teamUsers.put(3, new TeamUserInfo(UserPermissions.READ, teamPassword)); //user 3 has read access to team 1

        //Entries 1 and 2 are part of team 1
        teamEntries = new ArrayList<>();
        teamEntries.add(1);
        teamEntries.add(2);


        testUser = new AegisUser(new UserInfo(userFirstName, userLastName, hashedMasterPassword,
                masterPasswordSalt, encKeyDerivationSalt, macKeyDerivationSalt, publicKey, publicKeyMAC, keyIV, privateKey),
                userEmail, userAccess, testID);
        testEntry = new AegisEntry(new EntryInfo(entryTitle, entryUsername, entryPassword, iv), entryTeam, testID);
        testTeam = new AegisTeam(new TeamInfo(teamName), teamEntries, teamUsers, testID);
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

    @Test
    public void testClearDatabase() throws Exception {
        db.reset();

        //Basically every database operation should fail
        assertFalse(db.readUser(userEmail).isPresent());
        assertFalse(db.readUser(testID).isPresent());
        assertFalse(db.readEntry(testID).isPresent());
        assertFalse(db.readTeam(testID).isPresent());

        assertFalse(db.updateUser(testUser));
        assertFalse(db.updateEntry(testEntry));
        assertFalse(db.updateTeam(testTeam));
    }

    @Test
    public void checkIfUserExists() throws Exception {
        assertFalse(db.readUser(userEmail).isPresent());

        db.createUser(testUser);

        assertTrue(db.readUser(userEmail).isPresent());
    }

    @Test
    public void testMultipleEmail() throws Exception {
        db.createUser(testUser);

        //Should reject creating another user with the same email
        testUser = new AegisUser(testUser.userInfo.withFirstNameAndLastName("new first", "new last"), userEmail, userAccess, testID);
        assertFalse(db.createUser(testUser).isPresent());

        //Accept user with a different email
        testUser = new AegisUser(testUser.userInfo, userEmail + "a", userAccess, testID);
        assertTrue(db.createUser(testUser).isPresent());
    }

    @Test
    public void testReadAegisUser() throws Exception {
        assertFalse(db.readUser(userEmail).isPresent());
        assertFalse(db.readUser(testID).isPresent());

        db.createUser(testUser);

        Optional<AegisUser> u1 = db.readUser(userEmail);
        Optional<AegisUser> u2 = db.readUser(testID);

        assertTrue(u1.isPresent());
        assertTrue(u2.isPresent());

        assertEquals(testUser, u1.get());
        assertEquals(testUser, u2.get());
        assertEquals(u1.get(), u2.get());
    }

    @Test
    public void testReadAegisEntry() throws Exception {
        assertFalse(db.readEntry(testID).isPresent());

        db.createEntry(testEntry);

        assertTrue(db.readEntry(testID).isPresent());

        assertEquals(testEntry, db.readEntry(testID).get());
    }

    @Test
    public void testReadAegisTeam() throws Exception {
        assertFalse(db.readTeam(testID).isPresent());

        db.createTeam(testTeam);

        assertTrue(db.readTeam(testID).isPresent());

        assertEquals(testTeam, db.readTeam(testID).get());
    }

    @Test
    public void testDeleteAegisUser() throws Exception {
        db.createUser(testUser);
        db.deleteUser(testID);
        assertFalse(db.readUser(testID).isPresent());
    }

    @Test
    public void testDeleteAegisTeam() throws Exception {
        db.createTeam(testTeam);
        db.deleteTeam(testID);
        assertFalse(db.readTeam(testID).isPresent());
    }

    @Test
    public void testDeleteAegisEntry() throws Exception {
        db.createEntry(testEntry);
        db.deleteEntry(testID);
        assertFalse(db.readEntry(testID).isPresent());
    }

    @Test
    public void testUpdateAegisUser() throws Exception {
        db.createUser(testUser);
        AegisUser au = db.readUser(testID).get();
        assertEquals(testUser, au);

        //Change a few aspects
        userAccess.put(100, new TeamUserInfo(UserPermissions.ADMIN, teamPassword));
        userAccess.put(1, new TeamUserInfo(UserPermissions.READ, teamPassword));
        userAccess.remove(2);
        testUser = new AegisUser(au.userInfo.withFirstNameAndLastName("new first", "new last"), userEmail + "a", userAccess, testID);

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

        //Change a few aspects
        testEntry = new AegisEntry(new EntryInfo(entryTitle + "h", entryUsername + "e", entryPassword + "l", iv + "iv"), entryTeam + 1, testID);

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

        //Change a few aspects
        teamEntries.remove(0);
        teamEntries.add(100);
        teamEntries.add(20);
        teamUsers.put(100, new TeamUserInfo(UserPermissions.ADMIN, teamPassword));
        teamUsers.put(1, new TeamUserInfo(UserPermissions.READ, teamPassword));
        teamUsers.remove(2);
        testTeam = new AegisTeam(new TeamInfo(teamName + "hello"), teamEntries, teamUsers, testID);

        at = db.readTeam(testID).get();
        assertNotEquals(testTeam, at);

        db.updateTeam(testTeam);
        at = db.readTeam(testID).get();
        assertEquals(testTeam, at);
    }
}
