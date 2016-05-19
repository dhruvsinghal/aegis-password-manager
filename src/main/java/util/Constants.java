package main.java.util;

/**
 * A folder for all the constants in our project
 */
public class Constants {
    /**
     * The name of our main database that stores users, teams, and entries.
     */
    public static final String DB_NAME = "main.db";

    /**
     * Default port number for our server to start up on.
     */
    public static final int DEFAULT_PORT = 4567;

    /**
     * Flag for if creating a new user requires a verification code sent to the email. This defaults to true.
     * <p>
     * This flag is so that we can set this to false in our test cases.
     */
    public static boolean verifyFlag = true;


    /**
     * The maximum amount of threads our server supports
     */
    public static final int MAX_THREADS = 50;


    /**
     * The name of the configuration
     */
    public static final String CONFIG_FILE = "config.json";


    /**
     * Message to send user if we are unable to log them in.
     */
    public static final String FAILED_LOGIN_MESSAGE = "Unable to authenticate this username/password pair";

    /**
     * Message to send user if we are unable to log them in.
     */
    public static final String FAILED_DELETE_ONLY_ADMIN_MESSAGE = "You cannot remove yourself from the team being since you are the only admin";

    /**
     * Maximum amount of time a session ID is allowed to be inactive.
     * Measured in milliseconds.
     */
    public static final long MAX_INACTIVE_TIME = 1000 * 60 * 10; //Currently set to 10 minutes

    /**
     * Maximum amount of time you have to use a verification code
     * Measured in milliseconds.
     */
    public static final long MAX_VERIFICATION_TIME = 1000 * 60 * 10; //Currently set to 10 minutes
}
