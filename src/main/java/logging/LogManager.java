package main.java.logging;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * The primary class responsible for managing the log database.
 */
public class LogManager {
    /**
     * The name of the log database file. Note if you wish to change it you must also change the name
     * in the hibernate configuration file. Please leave this private to this class since
     * all logging operations should be controlled here.
     */
    private static final String dbFileName = "log.db";

    /**
     * The name of the entity
     */
    private static final String entityName = "LogEntry";

    /**
     * The session factory this LogManager uses
     */
    private final SessionFactory factory;

    /**
     * Creates a new LogManager. Reset defaults to false
     */
    public LogManager() {
        this(false);
    }


    /**
     * Creates a new LoggerManager.
     *
     * @param reset true iff we should delete the database to reset it.
     */
    public LogManager(boolean reset) {
        if (reset) { //SQLite so just delete the file if it exists
            try {
                Files.deleteIfExists(new File(dbFileName).toPath());
            } catch (IOException e) { //File probably didn't exist. Not a big deal
                e.printStackTrace();
            }
        }

        factory = new Configuration().configure().addAnnotatedClass(LogEntry.class).buildSessionFactory();
    }

    /**
     * Close this logger manager
     */
    public void close() {
        factory.close();
    }

    /**
     * Adds the given log entry into the database
     * <p>
     * The log entry must fulfill the spec of LogEntry
     */
    public void log(LogEntry entry) {
        try (Session session = factory.openSession()) {
            Transaction t = session.beginTransaction();
            session.save(entry);
            t.commit();
        }
    }

    /**
     * Method for logging
     *
     * @param level   Log level
     * @param message the message
     * @param ip      the ip address
     */
    public void log(@NotNull LogLevel level, @NotNull String message, @NotNull String ip) {
        log(level, message, ip, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Method for logging
     *
     * @param level   Log level
     * @param message the message
     * @param ip      the ip address to log
     * @param uid     optional userid
     * @param tid     optional teamid
     * @param eid     optional entryid
     */
    public void log(@NotNull LogLevel level, @NotNull String message, @NotNull String ip,
                    Optional<Integer> uid, Optional<Integer> tid, Optional<Integer> eid) {
        //Setup log entry
        LogEntry entry = new LogEntry(level, message);

        //Set fields
        entry.setIpAddress(ip);
        uid.ifPresent(entry::setUid);
        tid.ifPresent(entry::setTid);
        eid.ifPresent(entry::setEid);

        log(entry);
    }

    // ----------------- Queries ------------------------

    /**
     * @return A list of all entries in the log
     */
    @SuppressWarnings("unchecked") //We know that the session is unchecked, it should still be safe
    public List<LogEntry> getAllEntries() {
        try (Session session = factory.openSession()) {
            return session.createQuery("from " + entityName).list();
        }
    }

    /**
     * @param uid the uid of the user we want to get the logs for
     * @return A list of all log entries regarding the given user
     */
    @SuppressWarnings("unchecked") //We know that the session is unchecked, it should still be safe
    public List<LogEntry> getEntriesbyUid(int uid) {
        try (Session session = factory.openSession()) {
            String hql = "FROM " + entityName + " E WHERE E.uid = :uid";
            Query query = session.createQuery(hql);
            query.setParameter("uid", uid);
            return query.list();
        }
    }

    /**
     * @param tid the tid of the team we want to get the logs for
     * @return A list of all log entries regarding the given team
     */
    @SuppressWarnings("unchecked") //We know that the session is unchecked, it should still be safe
    public List<LogEntry> getEntriesbyTid(int tid) {
        try (Session session = factory.openSession()) {
            String hql = "FROM " + entityName + " E WHERE E.tid = :tid";
            Query query = session.createQuery(hql);
            query.setParameter("tid", tid);
            return query.list();
        }
    }

    //TODO add in queries for various elements as necessary.
}