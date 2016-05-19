package main.java.server;

import main.java.database.AegisPasswordDatabase;
import main.java.database.PasswordDatabase;
import main.java.logging.LogManager;
import main.java.server.config.ConfigHandler;
import main.java.server.config.Configuration;
import main.java.util.Constants;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A class that will setup the password database for the server. Please run this before running the server.
 */
public class Install {
    /*
    These are fields for the configuration. Please adjust as necessary. We could easily change this to be arguments
    but it is easier to leave them as fields for now since we have to run this rather frequently.
    */
    private final static String email = "<redacted>";
    private final static String emailPassword = "<redacted>";
    private final static String keyStoreFile = "<redacted>";
    private final static String certificatePassword = "<redacted>";

    public static void main(String[] args) {
        install();
    }

    public static void install() {
        try {
            // Reset the database
            PasswordDatabase db = new AegisPasswordDatabase(Constants.DB_NAME);
            db.reset();

            // Reset the log
            LogManager lm = new LogManager(true);
            lm.close();

            // Write out the configuration
            Configuration config = new Configuration(email, emailPassword, keyStoreFile, certificatePassword);
            ConfigHandler.writeConfiguration(config);

            System.out.println("Installation Complete And Successful!");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
