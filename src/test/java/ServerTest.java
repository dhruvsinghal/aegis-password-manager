package test.java;

import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import main.java.database.AegisPasswordDatabase;
import main.java.server.Server;
import main.java.util.Constants;
import org.junit.*;
import spark.utils.IOUtils;

import java.io.*;
import java.net.*;
import java.security.Security;
import java.util.List;
import java.util.Map;

import static java.net.CookieHandler.setDefault;
import static org.junit.Assert.*;

/**
 * Server test cases.
 * Need to set verifyFlag to false in Constants file for testing.
 */
public class ServerTest {
    private static final int PORT = Constants.DEFAULT_PORT;
    private static final String EMAIL = "test@test.test";
    private static final String EMAIL2 = "test2@test.test";
    private static final String PASSWORD = "password";
    private static final String FIRSTNAME = "First";
    private static final String LASTNAME = "Last";
    private static final String TEAMNAME = "BestTeam";
    private static final String ENTRYTITLE = "BestEntry";
    private static final String ENTRYTITLE2 = "BestEntry2";
    private static final String ENTRYUSERNAME = "BestEntryUsername";
    private static final String ENTRYUSERNAME2 = "BestEntryUsername2";
    private static final String ENTRYPASSWORD = "BestEntryPassword";
    private static final String ENTRYPASSWORD2 = "BestEntryPassword2";
    private static final String HASHEDMASTERPASSWORD = "hashedMasterPassword";
    private static final String MASTERPASSWORDSALT = "masterPasswordSalt";
    private static final String ENCKEYDERIVATIONSALT = "encKeyDerivationSalt";
    private static final String MACKEYDERIVATIONSALT = "macKeyDerivationSalt";
    private static final String PUBLICKEY = "publicKey";
    private static final String PUBLICKEYMAC = "publicKeyMAC";
    private static final String ENCIV = "encIv";
    private static final String PRIVATEKEY = "privateKey";
    private static final String ENCRYPTEDTEAMKEY = "encryptedTeamKey";
    private static final String IV = "Iv";
    private static final String IV2 = "Iv2";

    ///////////////////////////////////////////////////////////////////////////
    // JUnit Setup Methods
    ///////////////////////////////////////////////////////////////////////////
    @BeforeClass
    public static void setUpServer() throws Exception {
        //TODO fix these tests to be able to use the mail
        //Run the server on a separate thread
        Constants.verifyFlag = false;
        new Thread(() -> {
            try {
                Constants.verifyFlag = false;
                Server.main(new String[0]);
            } catch (IOException e) {
                System.err.println("Unable to startup server");
                e.printStackTrace();
            }
        }).start();

        //Wait 5 seconds for the server to startup
        Thread.sleep(5000);
    }

    @AfterClass
    public static void tearDownServer() throws Exception {
        //Wait 5 seconds for the server to startup
        Thread.sleep(5000);

        Server.stopServer();
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("javax.net.ssl.trustStorePassword","drsmmsrd");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
        System.setProperty("javax.net.ssl.trustStore","./truststore.jks");
//        System.setProperty("javax.net.debug","all");

        /**
         * Transport Security (SSL) Workaround for your “localhost” development environment
         */
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
                (hostname, sslSession) -> {
                    if (hostname.equals("localhost")) {
                        return true;
                    }
                    return false;
                });
    }

    @After
    public void tearDown() throws Exception {
        AegisPasswordDatabase db = new AegisPasswordDatabase(Constants.DB_NAME);
        db.reset();
        db.close();
    }

    ///////////////////////////////////////////////////////////////////////////
    // JUnit Test Cases
    ///////////////////////////////////////////////////////////////////////////

    //-------------------- User resource test cases ----------------------------

    @Test
    public void canGetHomepageTest() {
        UrlResponse res = doMethod("GET", "/", "", "");

        assertResponseNotNull(res);
        System.out.println("GetHomepageTest succeeded");
    }

    @Test
    public void canCreateUserTest() {
        Integer userId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        System.out.println("CreateUserTest succeeded: "+userId);
    }

    @Test
    public void canLoginUserTest() {
        Integer userId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        System.out.println("LoginUserTest succeeded: "+loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT));
    }

    @Test
    public void canUpdateUserTest() {
        Integer userId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        JsonObject jo = new JsonObject();
        jo.addProperty("email", EMAIL);
        jo.addProperty("firstName", FIRSTNAME);
        jo.addProperty("lastName", LASTNAME);
        UrlResponse res = doMethod("PUT", "/users", jo.toString(), "sessionID="+sessionID);

        assertResponseNotNull(res);
        System.out.println("UpdateUserTest succeeded: "+res.body);
    }

    @Test
    public void canDeleteUserTest() {
        Integer userId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        UrlResponse res = doMethod("DELETE", "/users", "", "sessionID="+sessionID);

        assertNotNull(res);
        assertNotNull(res.body);
        assertEquals(200, res.status);
    }


    //-------------------- Team resource test cases ----------------------------
    @Test
    public void canGetTeamsListTest() {
        Integer userId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        UrlResponse res = doMethod("GET", "/teams", "", "sessionID="+sessionID);

        assertResponseNotNull(res);
        System.out.println("GetTeamsListTest succeeded: "+res.body);
    }

    @Test
    public void canReadUsersListTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        addUserToTeam(EMAIL2, teamId, "READ", sessionID, addedUserId, ENCRYPTEDTEAMKEY);

        UrlResponse res = doMethod("GET", "/teams/"+teamId, "", "sessionID="+sessionID);

        assertResponseNotNull(res);
        System.out.println("ReadUsersListTest succeeded: "+res.body);
    }

    @Test
    public void canAddUserToTeamTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        addUserToTeam(EMAIL2, teamId, "READ", sessionID, addedUserId, ENCRYPTEDTEAMKEY);

        System.out.println("AddUserToTeamTest succeeded");
    }

    @Test
    public void canChangeUserPermissionOnTeamTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        addUserToTeam(EMAIL2, teamId, "READ", sessionID, addedUserId, ENCRYPTEDTEAMKEY);

        JsonObject jo = new JsonObject();
        jo.addProperty("uid", addedUserId);
        jo.addProperty("tid", teamId);
        jo.addProperty("permissions", "WRITE");
        UrlResponse res = doMethod("POST", "/teams/modify/permissions", jo.toString(), "sessionID="+sessionID);

        assertResponseNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);

        System.out.println("ChangeUserPermissionOnTeamTest succeeded");
    }

    @Test
    public void canDeleteUserFromTeamTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        addUserToTeam(EMAIL2, teamId, "READ", sessionID, addedUserId, ENCRYPTEDTEAMKEY);

        UrlResponse res = doMethod("DELETE",
                "/teams/"+teamId+"/users/"+addedUserId, "", "sessionID="+sessionID);

        assertResponseNotNull(res);

        System.out.println("DeleteUserFromTeamTest succeeded");
    }

    @Test
    public void canCreateTeamTest() {
        Integer userId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY);

        System.out.println("CreateTeamTest succeeded");
    }

    //-------------------- Entry resource test cases ---------------------------
    @Test
    public void canReadEntriesListTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        UrlResponse res = doMethod("GET", "/teams/"+teamId+"/entries", "", "sessionID="+sessionID);

        assertResponseNotNull(res);

        System.out.println("ReadEntriesListTest succeeded: "+res.body);
    }

    @Test
    public void canUpdateEntryTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        Integer entryId = Integer.valueOf(
                createEntry(teamId, ENTRYTITLE, ENTRYUSERNAME, ENTRYPASSWORD, IV, sessionID));

        // Non-secure parts
        JsonObject jo = new JsonObject();
        jo.addProperty("title", ENTRYTITLE2);
        jo.addProperty("username", ENTRYUSERNAME2);
        UrlResponse res = doMethod("POST", "/teams/"+teamId+"/entries/"+entryId+"/modify", jo.toString(), "sessionID="+sessionID);

        assertResponseNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);

        // Secure parts
        JsonObject jo2 = new JsonObject();
        jo2.addProperty("password", ENTRYPASSWORD2);
        jo2.addProperty("iv", IV2);
        UrlResponse res2 = doMethod("POST", "/teams/"+teamId+"/entries/"+entryId+"/secure", jo2.toString(), "sessionID="+sessionID);

        assertResponseNotNull(res2);
        assertEquals(200, res2.status);
        assertNotNull(res2.body);

        System.out.println("UpdateEntryTest succeeded: ");
    }

    @Test
    public void canDeleteEntryTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        Integer entryId = Integer.valueOf(
                createEntry(teamId, ENTRYTITLE, ENTRYUSERNAME, ENTRYPASSWORD, IV, sessionID));

        UrlResponse res = doMethod("DELETE", "/teams/"+teamId+"/entries/"+entryId, "", "sessionID="+sessionID);

        assertResponseNotNull(res);

        System.out.println("DeleteEntryTest succeeded");
    }

    @Test
    public void canCreateEntryTest() {
        Integer mainUserId = Integer.valueOf(createUser(EMAIL, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        Integer addedUserId = Integer.valueOf(createUser(EMAIL2, PASSWORD, FIRSTNAME, LASTNAME, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT, ENCKEYDERIVATIONSALT, MACKEYDERIVATIONSALT, PUBLICKEY, PUBLICKEYMAC, ENCIV, PRIVATEKEY));
        UrlResponse res_login = loginUser(EMAIL, PASSWORD, HASHEDMASTERPASSWORD, MASTERPASSWORDSALT);
        JsonObject login_res_jo = (new JsonParser()).parse(res_login.body).getAsJsonObject();
        String sessionID = res_login.sessionID;

        Integer teamId = Integer.valueOf(createTeam(TEAMNAME, sessionID, ENCRYPTEDTEAMKEY));
        Integer entryId = Integer.valueOf(
                createEntry(teamId, ENTRYTITLE, ENTRYUSERNAME, ENTRYPASSWORD, IV, sessionID));

        System.out.println("CreateEntryTest succeeded: "+entryId);
    }


    ///////////////////////////////////////////////////////////////////////////
    // helper methods and classes
    ///////////////////////////////////////////////////////////////////////////

    //------------------- JUnit test case helpers ------------------------------

    private static String createUser(String email, String password, String firstName, String lastName,
                                     String hashedMasterPassword, String masterPasswordSalt,
                                     String encKeyDerivationSalt, String macKeyDerivationSalt,
                                     String publicKey, String publicKeyMAC, String encIv, String privateKey) {
        JsonObject jo = new JsonObject();
        jo.addProperty("email", email);
        UrlResponse res = doMethod("POST", "/verification", jo.toString(), "");

        assertNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);

        JsonObject jo2 = new JsonObject();
        jo2.addProperty("code", email);
        jo2.addProperty("firstName", firstName);
        jo2.addProperty("lastName", lastName);
        jo2.addProperty("hashedMasterPassword", hashedMasterPassword);
        jo2.addProperty("masterPasswordSalt", masterPasswordSalt);
        jo2.addProperty("encKeyDerivationSalt", encKeyDerivationSalt);
        jo2.addProperty("macKeyDerivationSalt", macKeyDerivationSalt);
        jo2.addProperty("publicKey", publicKey);
        jo2.addProperty("publicKeyMAC", publicKeyMAC);
        jo2.addProperty("encIv", encIv);
        jo2.addProperty("privateKey", privateKey);

        UrlResponse res2 = doMethod("POST", "/users", jo2.toString(), "");

        assertNotNull(res2);
        assertEquals(200, res2.status);
        assertNotNull(res2.body);
        return res2.body;
    }

    // password field is unused right now because we only compare hashed master password sent earlier to what is received and do not test encryption/decryption
    private static UrlResponse loginUser(String email, String password, String hashedMasterPassword, String masterPasswordSalt) {
        JsonObject jo = new JsonObject();
        jo.addProperty("email", EMAIL);

        UrlResponse res = doMethod("POST", "/login1", jo.toString(), "");

        assertNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);

        JsonObject login_res_jo = (new JsonParser()).parse(res.body).getAsJsonObject();
        String salt = login_res_jo.get("salt").getAsString();

        assertEquals(salt, masterPasswordSalt);

        JsonObject jo2 = new JsonObject();
        jo2.addProperty("email", email);
        jo2.addProperty("hashedMasterPassword", hashedMasterPassword);
        jo2.addProperty("code", email);

        UrlResponse res2 = doMethod("POST", "/login2", jo2.toString(), "");

        assertNotNull(res2);
        assertEquals(200, res2.status);
        assertNotNull(res2.body);
        return res2;
    }

    private static String createTeam(String teamname, String sessionID, String teamKey) {
        JsonObject jo = new JsonObject();
        jo.addProperty("teamName", teamname);
        jo.addProperty("teamKey", teamKey);
        UrlResponse res = doMethod("POST", "/teams", jo.toString(), "sessionID="+sessionID);

        assertNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);
        return res.body;
    }

    private static void addUserToTeam(String email, Integer teamId, String permission, String sessionID, Integer userId, String teamKey) {
        JsonObject jo = new JsonObject();
        jo.addProperty("uid", userId);
        jo.addProperty("tid", teamId);
        jo.addProperty("permissions", permission);
        jo.addProperty("teamKey", teamKey);
        UrlResponse res = doMethod("POST", "/teams/modify/adduser",
                jo.toString(), "sessionID="+sessionID);

        assertNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);
    }

    private static String createEntry(Integer teamId, String entrytitle, String entryusername,
                             String entrypassword, String iv, String sessionID) {
        JsonObject jo = new JsonObject();
        jo.addProperty("title", entrytitle);
        jo.addProperty("username", entryusername);
        jo.addProperty("password", entrypassword);
        jo.addProperty("iv", iv);
        UrlResponse res = doMethod("POST", "/teams/"+teamId+"/entries",
                jo.toString(), "sessionID="+sessionID);

        assertNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);
        return res.body;
    }

    private static void assertResponseNotNull(UrlResponse res) {
        assertNotNull(res);
        assertEquals(200, res.status);
        assertNotNull(res.body);
    }

    //----------------------- URL Connection helpers ---------------------------

    private static UrlResponse doMethod(String requestMethod, String path, String body, String cookie) {
        UrlResponse res = new UrlResponse();

        try {
            getResponse(requestMethod, path, body, cookie, res);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    private static void getResponse(String requestMethod, String path,
                                    String body, String cookie, UrlResponse res)
            throws IOException {
        setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
        URL url = new URL(null, "https://localhost:" + PORT + path,
                new sun.net.www.protocol.https.Handler());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        /**
         * Set connection properties.
         */
        conn.setDoOutput(true);
        conn.setRequestMethod(requestMethod);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Cookie", cookie);
        conn.setRequestProperty("charset", "UTF-8");
        if (! body.isEmpty()) conn.getOutputStream().write(body.getBytes("UTF-8"));

        conn.connect();

        String response = IOUtils.toString(conn.getInputStream());
        res.body = response;
        res.status = conn.getResponseCode();
        res.headers = conn.getHeaderFields();
        if (res.headers.containsKey("Set-Cookie")) {
            res.sessionID = res.headers.get("Set-Cookie").get(0).substring("sessionID=".length());
        }
        else {
            res.sessionID = "";
        }
    }

    private static class UrlResponse {
        public Map<String, List<String>> headers;
        public String sessionID;
        private String body;
        private int status;
    }
}
