package main.java.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import main.java.server.config.ConfigHandler;
import main.java.server.config.Configuration;
import main.java.server.json.JsonTransformer;
import main.java.server.json.request.entry.ReqChangeEntryNonSecure;
import main.java.server.json.request.entry.ReqChangeEntrySecure;
import main.java.server.json.request.entry.ReqCreateEntry;
import main.java.server.json.request.team.ReqAddUserToTeam;
import main.java.server.json.request.team.ReqChangeUserPermissions;
import main.java.server.json.request.team.ReqChangeUserTKey;
import main.java.server.json.request.team.ReqCreateTeam;
import main.java.server.json.request.user.*;
import main.java.server.util.RequestHandler;
import main.java.util.Constants;

import java.io.IOException;

import static spark.Spark.*;

public class Server {
    /**
     * @return the port to run this server on
     */
    public static int getPort() {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (processBuilder.environment().get("PORT") != null) {
            return Integer.parseInt(processBuilder.environment().get("PORT"));
        }
        return Constants.DEFAULT_PORT; //return default port if heroku-port isn't set (i.e. on localhost)
    }

    /**
     * Stops the server from running. Useful for test cases
     */
    public static void stopServer() {
        stop();
    }

    /**
     * Main method for the server
     */
    public static void main(String[] args) throws IOException {
        // --------------------------- Setup ----------------------------
        final RequestHandler requestHandler;
        final Configuration config;
        final Gson gson = new Gson();

        try {
            config = ConfigHandler.loadConfiguration();
            requestHandler = new RequestHandler(config);
        } catch (Exception e) {
            System.err.println("There was an issue starting up the server");
            e.printStackTrace();
            stop();
            return;
        }

        //For setting up the port later
        port(getPort());

        //Define number of threads
        threadPool(Constants.MAX_THREADS);

        //Set up ssl
        secure(config.keyStoreFile, config.keyStorePassword, null, null);

        //Setup location of our ui files
        staticFileLocation("/ui");

        //Set all our responses to be JSON type.
        after((req, res) -> res.type("application/json"));

        // ------------------------ Exceptions ------------------------
        /**
         * Some form of internal except that we do not want the user to know about
         */
        exception(RequestHandler.InternalException.class, (e, req, res) -> {
            res.status(500);
            res.body("Unable to process the request");
        });

        /**
         * An exception with a message to pass back to the user
         */
        exception(RequestHandler.MessageException.class, (e, req, res) -> {
            res.status(500);
            res.body(e.getMessage());
        });

        /**
         * Caused by a unauthorized access attempt
         */
        exception(RequestHandler.UnauthorizedException.class, (e, req, res) -> {
            res.status(401);
            res.body("Unauthorized. This incident has been reported");
        });

        /**
         * Caused by an unprivileged access attempt
         */
        exception(RequestHandler.RightsException.class, (e, req, res) -> {
            res.status(403);
            res.body(e.getMessage());
        });

        /**
         * Generally caused since we cannot parse the input as a json
         */
        exception(JsonSyntaxException.class, (e, req, res) -> {
            res.status(412);
            res.body("Invalid Input Body");
        });

        /**
         * Generally caused since we cannot parse the input as a json
         */
        exception(Exception.class, (e, req, res) -> {
            res.status(500);
            res.body("Invalid Operation");
        });

        // --------------------------- Requests ---------------------------
        /**
         * Homepage request.
         */
        get("/", (req, res) -> {
            res.redirect("/index.html");
            return 0;
        });

        // --------------- Login requests ---------------
        /**
         * Get the salt for the master password for the login
         */
        post("/login1", (req, res) -> {
            ReqLogin1 json = gson.fromJson(req.body(), ReqLogin1.class);
            return requestHandler.login1(req, res, json);
        }, new JsonTransformer());

        /**
         * Get session ID for user: returns a cookie with the session id
         * and a FullUser JSON object
         */
        post("/login2", (req, res) -> {
            ReqLogin2 json = gson.fromJson(req.body(), ReqLogin2.class);
            return requestHandler.login2(req, res, json);
        }, new JsonTransformer());

        // --------------- User requests ---------------

        /**
         * Get the full user for yourself
         */
        get("/user", requestHandler::getFullUser, new JsonTransformer());

        /**
         * Get the partial user of the given email
         */
        get("/user/email/:email", (req, res) -> {
            String email = req.params(":email");
            return requestHandler.getPartialUser(req, res, email);
        }, new JsonTransformer());

        /**
         * Get the partial user of the given email
         */
        get("/user/uid/:uid", (req, res) -> {
            int uid = Integer.parseInt(req.params(":uid"));
            return requestHandler.getPartialUser(req, res, uid);
        }, new JsonTransformer());

        /**
         * Update user information that doesn't relate to security
         */
        put("/users", (req, res) -> {
            ReqUpdateUser json = gson.fromJson(req.body(), ReqUpdateUser.class);
            requestHandler.updateUserInfo(req, res, json);
            return "";
        }, new JsonTransformer());

        /**
         * Update user information regarding the master password
         */
        put("/users/master", (req, res) -> {
            ReqUpdateMasterPassword json = gson.fromJson(req.body(), ReqUpdateMasterPassword.class);
            requestHandler.updateUserMasterPassword(req, res, json);
            return "";
        }, new JsonTransformer());

        /**
         * Delete the user that corresponds to this session ID
         */
        delete("/users", (req, res) -> {
            requestHandler.deleteUser(req, res);
            return "";
        }, new JsonTransformer());

        /**
         * Create new user
         */
        post("/users", (req, res) -> {
            ReqCreateUser json = gson.fromJson(req.body(), ReqCreateUser.class);
            return requestHandler.createUser(req, res, json);
        }, new JsonTransformer());

        /**
         * Send user an email for verification while signing up and save the email and the verification
         * code in the request handler object.
         */
        post("/verification", (req, res) -> {
            ReqVerification json = gson.fromJson(req.body(), ReqVerification.class);
            requestHandler.generateVerificationEmail(req, res, json);
            return "";
        }, new JsonTransformer());


        // --------------- Team requests ---------------

        /**
         * Return all teams that the logged in user is a part of (teamid list).
         */
        get("/teams", requestHandler::getUserTeam, new JsonTransformer());

        /**
         * Return all users in the team if the logged-in user is part of the team
         * (JsonTeamUser list).
         */
        get("/teams/:teamid/users", (req, res) -> {
            Integer tid = Integer.parseInt(req.params(":teamid"));
            return requestHandler.getUsersInATeam(req, res, tid);
        }, new JsonTransformer());


        /**
         * Return the team information with respect to the current user.
         */
        get("/teams/:teamid", (req, res) -> {
            Integer tid = Integer.parseInt(req.params(":teamid"));
            return requestHandler.getTeam(req, res, tid);
        }, new JsonTransformer());

        /**
         * Add the specific user to the team
         */
        post("/teams/modify/adduser", (req, res) -> {
            ReqAddUserToTeam json = gson.fromJson(req.body(), ReqAddUserToTeam.class);
            requestHandler.addUserToTeam(req, res, json);
            return "";
        }, new JsonTransformer());

        /**
         * Changes the privileges of the user on the team
         */
        post("/teams/modify/permissions", (req, res) -> {
            ReqChangeUserPermissions json = gson.fromJson(req.body(), ReqChangeUserPermissions.class);
            requestHandler.changeUserPermissions(req, res, json);
            return "";
        }, new JsonTransformer());


        /**
         * Changes the team key of the specific user on the team.
         */
        post("/teams/modify/key", (req, res) -> {
            ReqChangeUserTKey json = gson.fromJson(req.body(), ReqChangeUserTKey.class);
            requestHandler.changeUserTeamkey(req, res, json);
            return "";
        }, new JsonTransformer());

        /**
         * Delete the specified user from the specified team.
         */
        delete("/teams/:teamid/users/:userid", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            int uid = Integer.parseInt(req.params(":userid"));
            requestHandler.deleteUserFromTeam(req, res, tid, uid);
            return "";
        }, new JsonTransformer());

        /**
         * Delete the whole team
         */
        delete("/teams/:teamid", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            requestHandler.deleteTeam(req, res, tid);
            return "";
        }, new JsonTransformer());

        /**
         * Create new team. Returns the team id
         */
        post("/teams", (req, res) -> {
            ReqCreateTeam json = gson.fromJson(req.body(), ReqCreateTeam.class);
            return requestHandler.createTeam(req, res, json);
        }, new JsonTransformer());

        // --------------- Entry requests ---------------

        /**
         * Return all entries that the logged in user has access to on
         * the specified team (entryid list).
         */
        get("/teams/:teamid/entries", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            return requestHandler.getEntries(req, res, tid);
        }, new JsonTransformer());


        /**
         * Change the secure parts of this entry (the password)
         */
        post("/teams/:teamid/entries/:entryid/secure", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            int eid = Integer.parseInt(req.params(":entryid"));
            ReqChangeEntrySecure json = gson.fromJson(req.body(), ReqChangeEntrySecure.class);
            requestHandler.editEntrySecure(req, res, tid, eid, json);
            return "";
        }, new JsonTransformer());

        /**
         * Change the non-secure fields (title, username)
         */
        post("/teams/:teamid/entries/:entryid/modify", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            int eid = Integer.parseInt(req.params(":entryid"));
            ReqChangeEntryNonSecure json = gson.fromJson(req.body(), ReqChangeEntryNonSecure.class);
            requestHandler.editEntryNonSecure(req, res, tid, eid, json);
            return "";
        }, new JsonTransformer());

        /**
         * Delete the entry if the logged in user has required permissions.
         */
        delete("/teams/:teamid/entries/:entryid", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            int eid = Integer.parseInt(req.params(":entryid"));
            requestHandler.deleteEntry(req, res, tid, eid);
            return "";
        }, new JsonTransformer());

        /**
         * Create a new entry in the specified team
         * Returns the entry id.
         */
        post("/teams/:teamid/entries", (req, res) -> {
            int tid = Integer.parseInt(req.params(":teamid"));
            ReqCreateEntry json = gson.fromJson(req.body(), ReqCreateEntry.class);
            return requestHandler.createEntry(req, res, tid, json);
        }, new JsonTransformer());


        // --------------- Utility requests ---------------

        /**
         * Returns true if the session is valid, false otherwise.
         */
        get("/sessionValid", requestHandler::isLoggedIn, new JsonTransformer());


        /**
         * Logs the user out of the session.
         */
        get("/logout", requestHandler::logOut, new JsonTransformer());


        // --------------- Log requests ---------------

        /**
         * Get logs for yourself
         */
        get("/logs/user", requestHandler::getUserLogs, new JsonTransformer());

        /**
         * Get logs by team id
         */

        get("/logs/teams/:teamid", (req, res) -> {
            Integer tid = Integer.parseInt(req.params(":teamid"));
            return requestHandler.getTeamLogs(req, res, tid);
        }, new JsonTransformer());
    }

}
