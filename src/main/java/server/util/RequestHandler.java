package main.java.server.util;

import main.java.database.AegisPasswordDatabase;
import main.java.database.PasswordDatabase;
import main.java.logging.LogEntry;
import main.java.logging.LogLevel;
import main.java.logging.LogManager;
import main.java.model.*;
import main.java.model.info.EntryInfo;
import main.java.model.info.TeamInfo;
import main.java.model.info.TeamUserInfo;
import main.java.model.info.UserInfo;
import main.java.server.config.Configuration;
import main.java.server.json.request.entry.ReqChangeEntryNonSecure;
import main.java.server.json.request.entry.ReqChangeEntrySecure;
import main.java.server.json.request.entry.ReqCreateEntry;
import main.java.server.json.request.team.ReqAddUserToTeam;
import main.java.server.json.request.team.ReqChangeUserPermissions;
import main.java.server.json.request.team.ReqChangeUserTKey;
import main.java.server.json.request.team.ReqCreateTeam;
import main.java.server.json.request.user.*;
import main.java.server.json.response.*;
import main.java.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.Request;
import spark.Response;

import java.util.*;

import static main.java.logging.LogConstants.SERVER_IP;
import static main.java.util.Constants.FAILED_LOGIN_MESSAGE;


/**
 * Class for handling server requests. All the methods in here are synchronized.
 */
public class RequestHandler {
    //Useful fields
    private final LogManager logger;
    private final PasswordDatabase db;
    private final SessionManager sidManager;
    private final EmailServer emailServer;
    private final VerificationCodeManager vcManager;

    //Configuration of this server
    private final Configuration config;

    /**
     * Construct a new request handler. This could run into issues.
     *
     * @param config The configuration of this server
     * @throws Exception An exception can be thrown if there are issues with initializations. This is probably
     *                   due to the log or database
     */
    public RequestHandler(Configuration config) throws Exception {
        this.config = config;
        logger = new LogManager();

        try {
            db = new AegisPasswordDatabase(Constants.DB_NAME);
            sidManager = new SessionManager();
            emailServer = new EmailServer(config.email, config.emailPassword, Optional.empty());
            vcManager = new VerificationCodeManager();

            //Log success
            logger.log(LogLevel.DEBUG, "Request Handler Setup Successful", SERVER_IP);
        } catch (Exception e) {
            //Log failure
            logger.log(LogLevel.DEBUG, "Request Handler Setup Failed", SERVER_IP);

            throw new Exception(e);
        }

    }

    // ---------------------------- Request Processing ----------------------------
    // Remember to make all these methods synchronized
    // ---------------------------- Login Related ----------------------------

    /**
     * Return the salt for a given user
     */
    public synchronized Salt login1(Request req, Response res, ReqLogin1 json) throws RequestException {
        Optional<AegisUser> user = db.readUser(json.email);

        if (user.isPresent()) {
            //Generate a verification code
            if (Constants.verifyFlag) {
                String code = vcManager.generateVerificationCode(json.email);

                //Send email
                EmailServer.Email vcEmail = EmailTemplates.generateLoginVerificationEmail(json.email, code);
                emailServer.sendEmail(vcEmail);

                logger.log(LogLevel.INFO, "Login verification code sent: " + code, req.ip());
            }

            return new Salt(user.get().userInfo.masterPasswordSalt);
        }

        logger.log(LogLevel.AUTH_ERROR, "Email did not exist: " + json.email, req.ip());
        throw new UnauthorizedException(FAILED_LOGIN_MESSAGE);

    }

    /**
     * Processes a login request and sets a cookie with the login request
     *
     * @return A full user object representing this user
     */
    public synchronized FullUser login2(Request req, Response res, ReqLogin2 json) throws RequestException {
        Optional<AegisUser> opUser = db.readUser(json.email);
        AegisUser user;

        //Check that user is present
        if (opUser.isPresent()) {
            user = opUser.get();
        } else {
            logger.log(LogLevel.AUTH_ERROR, "Email did not exist: " + json.email, req.ip());
            throw new UnauthorizedException(FAILED_LOGIN_MESSAGE);
        }

        //Check verification code
        Optional<String> optionalEmail = vcManager.getEmail(json.code);
        if (Constants.verifyFlag && (!optionalEmail.isPresent() ||
                !optionalEmail.get().equals(json.email))) {
            logger.log(LogLevel.AUTH_ERROR, "Invalid Verification Code", SERVER_IP,
                    Optional.empty(), Optional.empty(), Optional.empty());
            throw new MessageException("Invalid Verification Code. Your code may have expired.");
        }

        // Check hashed password equality
        if (!json.hashedMasterPassword.equals(user.userInfo.hashedMasterPassword)) {
            logger.log(LogLevel.AUTH_ERROR, "Incorrect Password", req.ip(), Optional.of(user.id),
                    Optional.empty(), Optional.empty());
            throw new UnauthorizedException(FAILED_LOGIN_MESSAGE);
        }

        //Generate a session id
        String sid = sidManager.generateSID(user.id);
        logger.log(LogLevel.INFO, "User Login Successful. Session ID: " + sid, req.ip(), Optional.of(user.id), Optional.empty(), Optional.empty());

        //Return
        res.cookie("sessionID", sid);
        return new FullUser(user);
    }


    // ---------------------------- User Related ----------------------------

    /**
     * Get a full user object of yourself
     */
    public synchronized FullUser getFullUser(Request req, Response res) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        return new FullUser(user);
    }

    /**
     * Get a partial user object of the given id.
     *
     * @param email the email you are trying to get the partial user of
     */
    public synchronized PartialUser getPartialUser(Request req, Response res, String email) throws RequestException {
        int myUID = getUIDFromSID(req);

        //Log this request
        logger.log(LogLevel.INFO, "Requesting user public info: " + email, req.ip(), Optional.of(myUID), Optional.empty(), Optional.empty());

        AegisUser user = getUserOptional(db.readUser(email), email, req.ip());
        return new PartialUser(user);
    }

    /**
     * Get a partial user object of the given id.
     *
     * @param uid the uid you are trying to get the partial user of
     */
    public synchronized PartialUser getPartialUser(Request req, Response res, int uid) throws RequestException {
        int myUID = getUIDFromSID(req);

        //Log this request
        logger.log(LogLevel.INFO, "Requesting user public info: " + uid, req.ip(), Optional.of(myUID), Optional.empty(), Optional.empty());

        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        return new PartialUser(user);
    }

    /**
     * Update the user information
     */
    public synchronized void updateUserInfo(Request req, Response res, ReqUpdateUser json) throws RequestException {
        int uid = getUIDFromSID(req);

        // Old user
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());

        //Setup new fields
        String firstName = getOrElseString(json.firstName, user.userInfo.firstName);
        String lastName = getOrElseString(json.lastName, user.userInfo.lastName);
        String email = getOrElseString(json.email, user.email);

        // Update the user
        boolean b = db.updateUser(user.withEmail(email)
                .withUserInfo(user.userInfo.withFirstNameAndLastName(firstName, lastName)));

        //Log
        if (b) {
            logger.log(LogLevel.INFO, "User Info Update Successful", req.ip(), Optional.of(user.id),
                    Optional.empty(), Optional.empty());
        } else {
            logger.log(LogLevel.ERROR, "User Info Update Failed", req.ip(), Optional.of(user.id),
                    Optional.empty(), Optional.empty());
        }

        throwInternalException(b);
    }

    /**
     * Update the user information regarding the master password
     */
    public synchronized void updateUserMasterPassword(Request req, Response res, ReqUpdateMasterPassword json) throws RequestException {
        int uid = getUIDFromSID(req);

        // Old user
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());

        // Update the user
        boolean b = db.updateUser(user.withUserInfo(user.userInfo.withMasterPasswordUpdate(json)));

        //Log
        if (b) {
            logger.log(LogLevel.WARNING, "Master Password Update Successful", req.ip(), Optional.of(user.id),
                    Optional.empty(), Optional.empty());
        } else {
            logger.log(LogLevel.ERROR, "Master Password Update Failed", req.ip(), Optional.of(user.id),
                    Optional.empty(), Optional.empty());
        }

        throwInternalException(b);
    }

    /**
     * Delete the user corresponding to this session ID
     */
    public synchronized void deleteUser(Request req, Response res) throws RequestException {
        int uid = getUIDFromSID(req);

        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());

        checkAdminDelete(req, user, Optional.empty());

        boolean b = db.deleteUser(uid);
        if (b) {
            logger.log(LogLevel.INFO, "User Delete Successful", req.ip(), Optional.of(uid),
                    Optional.empty(), Optional.empty());
        } else {
            logger.log(LogLevel.ERROR, "User Delete Failed", req.ip(), Optional.of(uid),
                    Optional.empty(), Optional.empty());
        }

        throwInternalException(b);
    }

    /**
     * Send a verification email for user creation
     */
    public synchronized void generateVerificationEmail(Request req, Response res, ReqVerification json) throws RequestException {
        if (db.readUser(json.email).isPresent()) {
            logger.log(LogLevel.INFO, "Username already exists", req.ip());
            throw new MessageException("Username already exists");
        }

        String code;
        if (Constants.verifyFlag) {
            code = vcManager.generateVerificationCode(json.email);

            //Send email
            EmailServer.Email vcEmail = EmailTemplates.generateVerificationEmail(json.email, code);
            emailServer.sendEmail(vcEmail);
        } else {
            //Just add the verification code in
            code = json.email;
            vcManager.addVerificationCode(code, json.email);
        }

        logger.log(LogLevel.INFO, "Verification code sent: " + code, req.ip());
    }

    /**
     * Attempts to create the user
     */
    public synchronized int createUser(Request req, Response res, ReqCreateUser json) throws RequestException {
        Optional<String> optionalEmail = vcManager.getEmail(json.code);
        if (!optionalEmail.isPresent()) {
            logger.log(LogLevel.WARNING, "Invalid Verification Code", SERVER_IP,
                    Optional.empty(), Optional.empty(), Optional.empty());
            throw new MessageException("Invalid Verification Code. Your code may have expired.");
        }

        //Setup the user
        String email = optionalEmail.get();
        NoIDAegisUser newUser = new NoIDAegisUser(
                new UserInfo(
                        json.firstName, json.lastName, json.hashedMasterPassword, json.masterPasswordSalt,
                        json.encKeyDerivationSalt, json.macKeyDerivationSalt, json.publicKey, json.publicKeyMAC,
                        json.encIv, json.privateKey
                ), email, new HashMap<>());

        //Try to add user
        Optional<Integer> userId = db.createUser(newUser);

        if (userId.isPresent()) {
            //send confirmation email
            if (Constants.verifyFlag) {
                EmailServer.Email confirmation = EmailTemplates.generateSignUpConfirmation(email, json.firstName);
                emailServer.sendEmail(confirmation);
            }

            //Log succcess!
            logger.log(LogLevel.INFO, "Successfully created new user:  " + email, SERVER_IP,
                    Optional.of(userId.get()), Optional.empty(), Optional.empty());
            vcManager.removeCode(json.code);
        } else {
            logger.log(LogLevel.WARNING, "Failed to create new user: " + email, SERVER_IP,
                    Optional.empty(), Optional.empty(), Optional.empty());
            throw new MessageException("Failed to create new user");
        }

        return getOptional(userId, LogLevel.ERROR, "Unable to create a new user", req.ip(), Optional.empty(),
                Optional.empty(), Optional.empty());
    }


    // ---------------------------- Team Related ----------------------------

    /**
     * Return all the Aegis Teams that the user is a part of
     */
    public synchronized ArrayList<Team> getUserTeam(Request req, Response res) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());

        //Return value
        ArrayList<Team> teams = new ArrayList<>();

        for (Map.Entry<Integer, TeamUserInfo> entry : user.teams.entrySet()) {
            int tid = entry.getKey();
            TeamUserInfo info = entry.getValue();
            AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());
            teams.add(new Team(tid, team.teamInfo.teamName, info.permissions, info.teamKey));
        }

        logger.log(LogLevel.INFO, "Teams belonging to user successfully returned to user", req.ip(),
                Optional.of(uid), Optional.empty(), Optional.empty());

        return teams;
    }

    /**
     * Return all Aegis users in the team if the logged-in user part of the team
     */
    public synchronized ArrayList<TeamUser> getUsersInATeam(Request req, Response res, int tid) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.READ);

        AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());

        ArrayList<TeamUser> users = new ArrayList<>();

        for (Map.Entry<Integer, TeamUserInfo> entry : team.users.entrySet()) {
            int entryUID = entry.getKey();
            TeamUserInfo info = entry.getValue();

            AegisUser u = getUserOptional(db.readUser(entryUID), entryUID, req.ip());
            TeamUser ju = new TeamUser(u, info.permissions, info.teamKey);
            users.add(ju);
        }

        logger.log(LogLevel.INFO, "Users in team successfully returned to user", req.ip(),
                Optional.of(uid), Optional.of(tid), Optional.empty());
        return users;

    }

    /**
     * Return team information.
     */
    public synchronized Team getTeam(Request req, Response res, int tid) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.READ);

        AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());
        @Nullable TeamUserInfo info = team.users.get(uid);

        if(info == null){
            throwInternalException(false);
        }

        Team teamResponse = new Team(team.id, team.teamInfo.teamName, info.permissions,
                info.teamKey);

        logger.log(LogLevel.INFO, "Team successfully returned to user", req.ip(),
                Optional.of(uid), Optional.of(tid), Optional.empty());

        return teamResponse;
    }

    /**
     * Add the specified user to the specified team.
     */
    public synchronized void addUserToTeam(Request req, Response res, ReqAddUserToTeam json) throws RequestException {
        //check to make sure that the session id is valid
        int uid = getUIDFromSID(req);
        AegisUser currentUser = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, currentUser, json.tid, UserPermissions.ADMIN);

        //Double check user exists
        AegisUser user = getUserOptional(db.readUser(json.uid), json.uid, req.ip());
        AegisTeam team = getTeamOptional(db.readTeam(json.tid), json.tid, req.ip());

        Map<Integer, TeamUserInfo> users = new HashMap<>(team.users);
        if (users.containsKey(json.uid)) {
            throw new MessageException("This user is already on the team.");
        }

        users.put(json.uid, new TeamUserInfo(json.permissions, json.teamKey));

        boolean b = db.updateTeam(team.withUsers(users));
        if (b) {
            logger.log(LogLevel.INFO, "Added user " + user.email +
                            " to team " + team.teamInfo.teamName, req.ip(),
                    Optional.of(uid), Optional.of(json.tid), Optional.empty());

        } else {
            logger.log(LogLevel.ERROR, "Unable to add user " + user.email +
                            " to team " + team.teamInfo.teamName, req.ip(),
                    Optional.of(uid), Optional.of(json.tid), Optional.empty());
        }

        throwInternalException(b);
    }

    /**
     * Change user permissions of specified user on specified team.
     */
    public synchronized void changeUserPermissions(Request req, Response res, ReqChangeUserPermissions json) throws RequestException {
        //check to make sure that the session id is valid
        int uid = getUIDFromSID(req);
        AegisUser currentUser = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, currentUser, json.tid, UserPermissions.ADMIN);

        //Double check user exists
        AegisUser user = getUserOptional(db.readUser(json.uid), json.uid, req.ip());
        AegisTeam team = getTeamOptional(db.readTeam(json.tid), json.tid, req.ip());

        //Make sure we aren't removing the only admin
        if (!json.permissions.equals(UserPermissions.ADMIN)) {
            checkAdminDelete(req, user, Optional.of(team));
        }

        Map<Integer, TeamUserInfo> users = new HashMap<>(team.users);

        //Check that the user is in the team
        @Nullable TeamUserInfo info = users.get(json.uid);
        if (info == null) {
            logger.log(LogLevel.ERROR, "User was not in the team: " + json.uid, req.ip(), Optional.of(uid), Optional.of(json.tid), Optional.empty());
            throw new MessageException("User was not in the team");
        }


        users.put(json.uid, new TeamUserInfo(json.permissions, info.teamKey));
        boolean b = db.updateTeam(team.withUsers(users));
        if (b) {
            logger.log(LogLevel.INFO, "Change user " + user.email +
                            " permissions to team " + team.teamInfo.teamName, req.ip(),
                    Optional.of(uid), Optional.of(json.tid), Optional.empty());

        } else {
            logger.log(LogLevel.ERROR, "Unable to change user " + user.email +
                            " permissions to team " + team.teamInfo.teamName, req.ip(),
                    Optional.of(uid), Optional.of(json.tid), Optional.empty());
        }

        throwInternalException(b);
    }

    /**
     * Change the User's Team AES Key.
     */
    public synchronized void changeUserTeamkey(Request req, Response res, ReqChangeUserTKey json) throws RequestException {
        //check to make sure that the session id is valid
        int uid = getUIDFromSID(req);
        AegisUser currentUser = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, currentUser, json.tid, UserPermissions.ADMIN);

        //Double check user exists
        AegisUser user = getUserOptional(db.readUser(json.uid), json.uid, req.ip());
        AegisTeam team = getTeamOptional(db.readTeam(json.tid), json.tid, req.ip());

        //Check that the user is in the team
        Map<Integer, TeamUserInfo> users = new HashMap<>(team.users);
        @Nullable TeamUserInfo info = users.get(json.uid);
        if (info == null) {
            logger.log(LogLevel.ERROR, "User was not in the team: " + json.uid, req.ip(), Optional.of(uid), Optional.of(json.tid), Optional.empty());
            throw new MessageException("User was not in the team");
        }


        users.put(json.uid, new TeamUserInfo(info.permissions, json.teamKey));

        boolean b = db.updateTeam(team.withUsers(users));
        if (b) {
            logger.log(LogLevel.INFO, "Changed user " + user.email +
                            " key to team " + team.teamInfo.teamName, req.ip(),
                    Optional.of(uid), Optional.of(json.tid), Optional.empty());

        } else {
            logger.log(LogLevel.ERROR, "Unable to change user " + user.email +
                            " key to team " + team.teamInfo.teamName, req.ip(),
                    Optional.of(uid), Optional.of(json.tid), Optional.empty());
        }

        throwInternalException(b);
    }

    /**
     * Delete the specified user from the specified team.
     */
    public synchronized void deleteUserFromTeam(Request req, Response res, int tid, int uid) throws RequestException {
        int userId = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(userId), userId, req.ip());
        checkRights(req, user, tid, UserPermissions.ADMIN);

        AegisUser delUser = getUserOptional(db.readUser(uid), userId, req.ip());
        AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());

        checkAdminDelete(req, delUser, Optional.of(team));

        Map<Integer, TeamUserInfo> users = new HashMap<>(team.users);

        users.remove(uid);
        boolean b = db.updateTeam(team.withUsers(users));

        if (b) {
            logger.log(LogLevel.INFO, "User: " + delUser.email + " successfully deleted from team", req.ip(),
                    Optional.of(userId), Optional.of(tid), Optional.empty());

        } else {
            logger.log(LogLevel.WARNING, "User: " + delUser.email + " failed to delete from team", req.ip(),
                    Optional.of(userId), Optional.of(tid), Optional.empty());

        }

        throwInternalException(b);
    }

    /**
     * Delete the entire team
     */
    public synchronized void deleteTeam(Request req, Response res, int tid) throws RequestException {
        int userId = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(userId), userId, req.ip());
        checkRights(req, user, tid, UserPermissions.ADMIN);

        //Check that team exists
        AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());

        boolean b = db.deleteTeam(tid);

        if (b) {
            logger.log(LogLevel.INFO, "Successfully deleted team", req.ip(),
                    Optional.of(userId), Optional.of(tid), Optional.empty());
        } else {
            logger.log(LogLevel.WARNING, "Failed to delete team", req.ip(),
                    Optional.of(userId), Optional.of(tid), Optional.empty());

        }

        throwInternalException(b);
    }

    /**
     * Create a new team
     */
    public synchronized int createTeam(Request req, Response res, ReqCreateTeam json) throws RequestException {
        int uid = getUIDFromSID(req);

        // Add the logged in user to the team
        HashMap<Integer, TeamUserInfo> members = new HashMap<>();
        members.put(uid, new TeamUserInfo(UserPermissions.ADMIN, json.teamKey));

        return getOptional(db.createTeam(
                new NoIDAegisTeam(
                        new TeamInfo(json.teamName),
                        new ArrayList<>(),
                        members
                )), LogLevel.ERROR, "Could not create team " + json.teamName, req.ip(),
                Optional.of(uid), Optional.empty(), Optional.empty());
    }

    // ---------------------------- Entry Related ----------------------------

    /**
     * Return the full entry for all the entries in this team
     */
    public synchronized ArrayList<FullEntry> getEntries(Request req, Response res, int tid) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.READ);

        AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());

        ArrayList<Integer> eids = new ArrayList<>(team.entries);
        ArrayList<FullEntry> entries = new ArrayList<>();

        for (int eid : eids) {
            AegisEntry e = getEntryOptional(db.readEntry(eid), eid, req.ip());
            FullEntry je = new FullEntry(e);
            entries.add(je);
        }

        return entries;
    }

    /**
     * Edit the secure portions of an entry
     */
    public synchronized void editEntrySecure(Request req, Response res, int tid, int eid, ReqChangeEntrySecure json) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.WRITE);

        AegisEntry entry = getEntryOptional(db.readEntry(eid), eid, req.ip());

        boolean b = db.updateEntry(new AegisEntry(
                new EntryInfo(entry.entryInfo.title, entry.entryInfo.username, json.password, json.iv),
                entry.team, entry.id)
        );

        if (b) {
            logger.log(LogLevel.INFO, "Secure Entry Edit Successful", req.ip(),
                    Optional.of(uid), Optional.of(tid), Optional.of(eid));
        } else {
            logger.log(LogLevel.ERROR, "Secure Entry Edit Unsuccessful", req.ip(),
                    Optional.of(uid), Optional.of(tid), Optional.of(eid));
        }
    }

    /**
     * Edit the non-secure portions of an entry
     */
    public synchronized void editEntryNonSecure(Request req, Response res, int tid, int eid, ReqChangeEntryNonSecure json) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.WRITE);

        AegisEntry entry = getEntryOptional(db.readEntry(eid), eid, req.ip());
        String title = getOrElseString(json.title, entry.entryInfo.title);
        String username = getOrElseString(json.username, entry.entryInfo.username);

        boolean b = db.updateEntry(new AegisEntry(
                new EntryInfo(title, username, entry.entryInfo.password, entry.entryInfo.iv),
                entry.team, entry.id)
        );

        if (b) {
            logger.log(LogLevel.INFO, "Non-secure Entry Edit Successful", req.ip(),
                    Optional.of(uid), Optional.of(tid), Optional.of(eid));
        } else {
            logger.log(LogLevel.ERROR, "Non-secure Entry Edit Unsuccessful", req.ip(),
                    Optional.of(uid), Optional.of(tid), Optional.of(eid));
        }
    }

    /**
     * Delete the specified password entry on the team.
     */
    public synchronized void deleteEntry(Request req, Response res, int tid, int eid) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.ADMIN);

        boolean b = db.deleteEntry(eid);

        if (b) {
            logger.log(LogLevel.INFO, "Delete Entry Successful", req.ip(),
                    Optional.of(uid), Optional.of(tid), Optional.of(eid));
        } else {
            logger.log(LogLevel.ERROR, "Delete Entry Unsuccessful", req.ip(),
                    Optional.of(uid), Optional.of(tid), Optional.of(eid));
        }
    }

    /**
     * Create a new password entry in the team.
     */
    public synchronized int createEntry(Request req, Response res, int tid, ReqCreateEntry json) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.WRITE);

        return getOptional(db.createEntry(
                new NoIDAegisEntry(
                        new EntryInfo(json.title, json.username, json.password, json.iv),
                        tid
                )), LogLevel.ERROR, "Unable to create entry", req.ip(),
                Optional.of(uid), Optional.of(tid), Optional.empty());
    }

    // ---------------------------- Utility requests ----------------------------

    /**
     * Checks if the current cookie is still valid
     */
    public synchronized boolean isLoggedIn(Request req, Response res) throws RequestException {
        try {
            getUIDFromSID(req);
        } catch (RequestException e) {
            return false;
        }

        return true;
    }

    /**
     * handler for user sign out
     */
    public synchronized boolean logOut(Request req, Response res) throws RequestException {
        //check to make sure that the session id is valid
        int uid = getUIDFromSID(req);


        //Remove the session id
        String sid = Optional.ofNullable(req.cookie("sessionID")).orElse("");
        sidManager.removeSessionID(sid);
        logger.log(LogLevel.INFO, "User Signed out", req.ip(),
                Optional.of(uid), Optional.empty(), Optional.empty());

        return true;
    }

    /**
     * @return a list of the logs corresponding to the userid of the session
     */
    public List<LogEntry> getUserLogs(Request req, Response res) throws RequestException {
        int uid = getUIDFromSID(req);
        return logger.getEntriesbyUid(uid);
    }

    /**
     * @return a list of the logs corresponding to the teamid of the session
     */
    public List<LogEntry> getTeamLogs(Request req, Response res, int tid) throws RequestException {
        int uid = getUIDFromSID(req);
        AegisUser user = getUserOptional(db.readUser(uid), uid, req.ip());
        checkRights(req, user, tid, UserPermissions.ADMIN);

        return logger.getEntriesbyTid(tid);
    }

    // ------------------------------- helper methods -------------------------------

    /**
     * Throw exception if user's role grants required rights to perform an action.
     */
    private void checkRights(@NotNull Request req, @NotNull AegisUser user, int tid, @NotNull UserPermissions reqRole) throws RightsException {
        if (!user.teams.containsKey(tid)) {
            logger.log(LogLevel.ERROR, "User is not part of this team", req.ip(), Optional.of(user.id),
                    Optional.of(tid), Optional.empty());
            throw new RightsException("You are not part of the team in question");
        }

        UserPermissions role = user.teams.get(tid).permissions;
        if (role.level < reqRole.level) { // Check for sufficient rights
            logger.log(LogLevel.ERROR, "User does not have appropriate rights", req.ip(), Optional.of(user.id),
                    Optional.of(tid), Optional.empty());
            throw new RightsException("You do not have the required rights\nRequired: " + reqRole.name() + "\n" + "Your Rights: " + role.name());
        }
    }

    /**
     * Make sure you don't delete the only admin of a team.
     *
     * @param user         The user you wish to delete
     * @param teamOptional A team to check specifically. If not passed, checks all the users teams
     */
    private void checkAdminDelete(@NotNull Request req, @NotNull AegisUser user, @NotNull Optional<AegisTeam> teamOptional) throws RequestException {
        if (teamOptional.isPresent()) {
            // If team id has been specified, check if user cannot not be removed from the specific team
            AegisTeam team = teamOptional.get();
            @Nullable TeamUserInfo info = team.users.get(user.id);
            if (info != null && info.permissions.equals(UserPermissions.ADMIN)) {
                long admins = team.users.entrySet().stream().filter(e -> e.getValue().permissions.equals(UserPermissions.ADMIN)).count();
                if (admins < 2) {
                    logger.log(LogLevel.ERROR, "Cannot delete the only admin of a team", req.ip(), Optional.of(user.id),
                            Optional.of(team.id), Optional.empty());
                    throw new RightsException(Constants.FAILED_DELETE_ONLY_ADMIN_MESSAGE);
                }
            }
        } else {
            // Otherwise check if user is not the only admin on any of the teams they are part of
            for (int tid : user.teams.keySet()) {
                AegisTeam team = getTeamOptional(db.readTeam(tid), tid, req.ip());
                checkAdminDelete(req, user, Optional.of(team));
            }
        }

    }

    /**
     * Helper method for trying to get the value from an optional.
     * <p>
     * The parameters are the information to log if we are unable to get this optional value.
     *
     * @throws InternalException throws this exception if there is no optional value
     */
    private <T> T getOptional(@NotNull Optional<T> optional, @NotNull LogLevel level, @NotNull String message, @NotNull String ip,
                              @NotNull Optional<Integer> uid, @NotNull Optional<Integer> tid, @NotNull Optional<Integer> eid)
            throws MessageException {
        if (!optional.isPresent()) {
            logger.log(level, message, ip, uid, tid, eid);
            throw new MessageException(message);
        }

        return optional.get();
    }

    /**
     * Attempt unwrap the user. The parameters are for logging.
     */
    private AegisUser getUserOptional(Optional<AegisUser> optional, int uid, String ip) throws MessageException {
        return getOptional(optional, LogLevel.ERROR, "Unable to locate user.", ip, Optional.of(uid), Optional.empty(), Optional.empty());
    }

    /**
     * Attempt unwrap the user. The parameters are for logging.
     */
    private AegisUser getUserOptional(Optional<AegisUser> optional, String email, String ip) throws MessageException {
        return getOptional(optional, LogLevel.ERROR, "Unable to locate user: " + email, ip, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Attempt unwrap the team. The parameters are for logging.
     */
    private AegisTeam getTeamOptional(Optional<AegisTeam> optional, int tid, String ip) throws MessageException {
        return getOptional(optional, LogLevel.ERROR, "Unable to locate team.", ip, Optional.empty(), Optional.of(tid), Optional.empty());
    }

    /**
     * Attempt unwrap the entry. The parameters are for logging.
     */
    private AegisEntry getEntryOptional(Optional<AegisEntry> optional, int eid, String ip) throws MessageException {
        return getOptional(optional, LogLevel.ERROR, "Unable to locate entry.", ip, Optional.empty(), Optional.empty(), Optional.of(eid));
    }

    /**
     * Throws an InternalException if b evaluates to false
     */
    private void throwInternalException(boolean b) throws InternalException {
        if (!b) throw new InternalException();
    }


    /**
     * Attempts to get the user id from the session id cookie. If we cannot get a user, throws an exception
     *
     * @param req a spark request
     * @return the uid corresponding to this session
     */
    private int getUIDFromSID(Request req) throws SessionIDException {
        String sid = Optional.ofNullable(req.cookie("sessionID")).orElse("");
        return sidManager.getUID(sid).orElseThrow(() -> new SessionIDException(sid, req.ip()));
    }


    /**
     * Returns the given obj if it's not null, otherwise it returns the else item.
     * <p>
     * This method is specifically for strings, which should cover most of the inputs in JSON.
     */
    private @NotNull String getOrElseString(@Nullable String obj, @NotNull String orElse) {
        if (obj != null) {
            return obj;
        }

        return orElse;
    }


    // --------------------------------- Exceptions ---------------------------------

    /**
     * An exception that is thrown if there is any issue processing a request
     */
    public abstract class RequestException extends Exception {
        public RequestException() {
        }

        public RequestException(String message) {
            super(message);
        }

        public RequestException(String message, Throwable cause) {
            super(message, cause);
        }

        public RequestException(Throwable cause) {
            super(cause);
        }

        public RequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    /**
     * These exceptions include a message to be sent back to the client.
     */
    public class MessageException extends RequestException {
        public MessageException(String message) {
            super(message);
        }
    }

    /**
     * An exception that is thrown if there is an internal issue processing a request
     */
    public class InternalException extends RequestException {
        public InternalException() {
        }

        public InternalException(String message) {
            super(message);
        }

        public InternalException(Throwable cause) {
            super(cause);
        }
    }


    /**
     * An exception that is thrown if there is an issue with authorization
     */
    public class UnauthorizedException extends RequestException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    /**
     * An exception that is thrown if there is a bad session ID.
     * Automatically logs the bad session ID attempt too.
     */
    public class SessionIDException extends UnauthorizedException {
        public SessionIDException(String sid, String ip) {
            super("Invalid Session");
            logger.log(LogLevel.AUTH_ERROR, "Invalid SessionID: " + sid, ip, Optional.empty(), Optional.empty(), Optional.empty());
        }

    }

    /**
     * An exception that is thrown if the user's role does not grant rights to
     * perform an action.
     */
    public class RightsException extends RequestException {
        public RightsException(String message) {
            super(message);
        }
    }
}
