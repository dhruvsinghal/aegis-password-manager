package main.java.database;

import main.java.model.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An interface for our password database.
 * <p>
 * Note that we do not provide a way to change userID, entryID, or teamID. This is intentional since
 * those numbers are intended to be unique identifying numbers for each user, entry, or team.
 */
public interface PasswordDatabase {
    /**
     * This resets the database which means that all data will be wiped and the tables will be setup again.
     * <p>
     * Note that reset is not run as a transaction. This method should only be used extremely sparingly and probably
     * only during the first startup.
     */
    void reset();

    //USER METHODS

    /**
     * @param user a user with all the fields except the ID filled in.
     * @return the userID or an empty optional if we were unable to create the user
     */
    @NotNull Optional<Integer> createUser(@NotNull NoIDAegisUser user);

    /**
     * @param email the email corresponding to a user
     * @return a user with all the fields set or an empty optional if we are unable to read the user
     */
    @NotNull Optional<AegisUser> readUser(@NotNull String email);

    /**
     * @param id the user ID
     * @return a user with all the fields set or an empty optional if we are unable to read the user
     */
    @NotNull Optional<AegisUser> readUser(int id);

    /**
     * This will update all the fields of the given user, which is identifying by the id.
     *
     * @param user the user to update.
     * @return true of successful, otherwise false
     */
    boolean updateUser(@NotNull AegisUser user);

    /**
     * @param id the user ID
     * @return true if successful, otherwise false.
     */
    boolean deleteUser(int id);


    //ENTRY METHODS

    /**
     * @param entry a entry with all the fields except the ID filled in.
     * @return the entryID or an empty optional if we were unable to create the entry
     */
    @NotNull Optional<Integer> createEntry(@NotNull NoIDAegisEntry entry);

    /**
     * @param id the entry ID
     * @return a entry with all the fields set or an empty optional if we are unable to read the entry
     */
    @NotNull Optional<AegisEntry> readEntry(int id);

    /**
     * This will update all the fields of the given entry, which is identifying by the id.
     *
     * @param entry the entry to update.
     * @return true of successful, otherwise false
     */
    boolean updateEntry(@NotNull AegisEntry entry);

    /**
     * @param id the entry ID
     * @return true if successful, otherwise false.
     */
    boolean deleteEntry(int id);

    //TEAM METHODS

    /**
     * @param team a team with all the fields except the ID filled in.
     * @return the teamID or an empty optional if we were unable to create the team
     */
    @NotNull Optional<Integer> createTeam(@NotNull NoIDAegisTeam team);

    /**
     * @param id the team ID
     * @return a team with all the fields set or an empty optional if we are unable to read the team
     */
    @NotNull Optional<AegisTeam> readTeam(int id);

    /**
     * This will update all the fields of the given team, which is identifying by the id.
     *
     * @param team the team to update.
     * @return true of successful, otherwise false
     */
    boolean updateTeam(@NotNull AegisTeam team);

    /**
     * @param id the entry ID
     * @return true if successful, otherwise false.
     */
    boolean deleteTeam(int id);


}
