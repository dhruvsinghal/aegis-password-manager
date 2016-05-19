package test.database;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        AegisPasswordDatabaseTest.class,
        SQLInjectTest.class,
})

public class DatabaseTests {
}