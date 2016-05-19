package test.util;

import main.java.model.AegisUser;
import main.java.model.info.EntryInfo;
import main.java.model.info.TeamInfo;
import main.java.model.info.UserInfo;
import main.java.util.Serializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Quick class for serialization
 */
public class SerializerTest {
    @Test
    public void testSerialize() throws Exception {
        EntryInfo ae = new EntryInfo("password", "salt", "firstname", "lastname");
        byte[] serialized = Serializer.serialize(ae);
        EntryInfo de = Serializer.deserialize(serialized);

        assertEquals(ae, de);
    }

    @Test(expected=ClassCastException.class)
    public void testFailure() throws Exception {
        EntryInfo au = new EntryInfo("password", "salt", "firstname", "lastname");
        byte[] serialized = Serializer.serialize(au);
        TeamInfo de = Serializer.deserialize(serialized);

        fail("Should not be able to deserialize to wrong object");
    }
}