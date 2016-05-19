package test;

import main.java.logging.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Basic test cases to ensure the log manager is working.
 *
 * Note that that these tests will fail if the server is also running at the same time.
 */
public class LogManagerTest {
    private LogManager lm;

    @Before
    public void setUp() throws Exception {
        lm = new LogManager(true);
    }

    @After
    public void tearDown() throws Exception {
        lm.close();
    }

    @Test
    public void basicTest() throws Exception {
        LogEntry entry = new LogEntry(LogLevel.DEBUG, "message");
        lm.log(entry);

        for(LogEntry e : lm.getAllEntries()){
            assertEquals(entry.getLevel(), e.getLevel());
            assertEquals(entry.getMessage(), e.getMessage());
            assertEquals(1, e.getId()); //First entry, so should have ID of 1

            //Default values for the rest
            assertEquals(LogConstants.NO_USER, e.getUid());
            assertEquals(LogConstants.NO_TEAM, e.getTid());
            assertEquals(LogConstants.NO_IP, e.getIpAddress());
            assertNotNull(e.getTime());
        }
    }

    @Test
    public void basicTIDCheck() throws Exception {
        LogEntry entry = new LogEntry(LogLevel.DEBUG, "hello");
        entry.setTid(1);
        lm.log(entry);

        LogEntry e2 = new LogEntry(LogLevel.DEBUG, "world");
        lm.log(e2);

        for(LogEntry e : lm.getEntriesbyTid(1)){
            assertEquals(entry.getLevel(), e.getLevel());
            assertEquals(entry.getMessage(), e.getMessage());
            assertEquals(1, e.getId()); //First entry, so should have ID of 1

            //Make sure team is the right id
            assertEquals(1, e.getTid());

            //Default values for the rest
            assertEquals(LogConstants.NO_USER, e.getUid());
            assertEquals(LogConstants.NO_IP, e.getIpAddress());
            assertNotNull(e.getTime());
        }
    }

    @Test
    public void basicUIDCheck() throws Exception {
        LogEntry entry = new LogEntry(LogLevel.DEBUG, "hello");
        entry.setUid(1);
        lm.log(entry);

        LogEntry e2 = new LogEntry(LogLevel.DEBUG, "world");
        lm.log(e2);

        for(LogEntry e : lm.getEntriesbyUid(1)){
            assertEquals(entry.getLevel(), e.getLevel());
            assertEquals(entry.getMessage(), e.getMessage());
            assertEquals(1, e.getId()); //First entry, so should have ID of 1

            //Right UID value
            assertEquals(1, e.getUid());

            //Rest are default values
            assertEquals(LogConstants.NO_TEAM, e.getTid());
            assertEquals(LogConstants.NO_IP, e.getIpAddress());
            assertNotNull(e.getTime());
        }
    }
}