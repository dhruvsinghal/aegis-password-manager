package main.java.server.config;

import com.google.gson.Gson;
import main.java.util.Constants;
import main.java.util.FileOperations;

import java.io.IOException;

/**
 * Class for handling the configuration of our server. It reads and loads the json file
 */
public class ConfigHandler {

    private static final Gson gson = new Gson();

    /**
     * @param config the configuration we would like to write out into the file
     */
    public static void writeConfiguration(Configuration config) throws IOException {
        FileOperations.writeToFile(Constants.CONFIG_FILE, gson.toJson(config));
    }

    /**
     * @return a configuration JSON created from the config file
     * @throws IOException If we were unable to load up the configuration.
     */
    public static Configuration loadConfiguration() throws IOException {
        return gson.fromJson(FileOperations.readFileString(Constants.CONFIG_FILE), Configuration.class);
    }
}
