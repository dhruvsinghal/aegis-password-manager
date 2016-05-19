package test;

import main.java.server.Install;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import test.database.DatabaseTests;
import test.java.ServerTest;
import test.util.SerializerTest;

@RunWith(Suite.class)
@SuiteClasses({
        DatabaseTests.class,
        LogManagerTest.class,
        SerializerTest.class,
        ServerTest.class
})

public class TestAll {
    @BeforeClass
    public static void init() {
        Install.install();
    }
}