package main.java.server.util;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static main.java.util.Constants.MAX_INACTIVE_TIME;

/**
 * Class responsible for managing sessionIDs
 */
public class SessionManager {
    //Constants
    private static final int session_id_size = 130; //Size of a session id in bits

    //map from username to sessionID
    private final ConcurrentHashMap<String, SessionID> sessionIDs;

    //useful fields
    private static final SecureRandom random = new SecureRandom();


    /**
     * Constructor for session manager
     */
    public SessionManager() {
        sessionIDs = new ConcurrentHashMap<>();

        //Start the reaper thread for session ids
        new Thread(this::reaper).start();
    }

    /**
     * Reaper thread for removing inactive sessionIDs
     */
    private void reaper() {
        while (true) {
            for (Map.Entry<String, SessionID> entry : sessionIDs.entrySet()) {
                if (!isSessionIdValid(entry.getValue())) {
                    sessionIDs.remove(entry.getKey());
                }
            }

            //Sleep for a second so the reaper isn't constantly running
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Create a session ID for the given user id and add it to the hashmap to keep track of.
     *
     * @param uid the uid of the user
     * @return the session id as a string
     */
    public synchronized String generateSID(int uid) {
        SessionID sid = new SessionID(uid);
        sessionIDs.put(sid.sid, sid);
        return sid.sid;
    }

    /**
     * @param sessionID the session id as a string
     * @return the corresponding user id or empty optional if there is no valid session id.
     */
    public Optional<Integer> getUID(String sessionID) {
        Optional<SessionID> sid = Optional.ofNullable(sessionIDs.get(sessionID));
        sid.ifPresent(SessionID::refresh);
        return sid.map(x -> x.uid);
    }

    /**
     * Remove the given session ID, which effectively logs the user out.
     *
     * @param sessionID
     */
    public void removeSessionID(@NotNull String sessionID) {
        sessionIDs.remove(sessionID);
    }

    /**
     * @return a randomly generated session ID that is not currently being used
     */
    private String generateSID() {
        String ret;
        do {
            ret = new BigInteger(session_id_size, random).toString(32);
        } while (sessionIDs.containsKey(ret));

        return ret;
    }

    /**
     * @return if the given session id is valid
     */
    private boolean isSessionIdValid(SessionID sid) {
        return System.currentTimeMillis() - sid.lastActiveTime < MAX_INACTIVE_TIME;
    }

    /**
     * Inner class for a session ID
     */
    private class SessionID {
        private final int uid; //The user corresponding to this session id

        @NotNull
        private final String sid; //The session id

        private long lastActiveTime; //The last time this session ID was interacted with

        private SessionID(int uid) {
            this.uid = uid;
            this.sid = generateSID();
            this.lastActiveTime = System.currentTimeMillis();
        }

        /**
         * Refreshes this session ID to the current system time.
         */
        private void refresh() {
            lastActiveTime = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SessionID sessionID = (SessionID) o;

            return sid.equals(sessionID.sid);

        }

        @Override
        public int hashCode() {
            return sid.hashCode();
        }
    }
}
