# Aegis
The password manager for teams!

#### Code Locations
Our main class for the server is src/main/java/server/Server.java. However most of the interesting code is contained in the RequestHandler.java (which is in the same package).

All the UI code is contained in src/main/resources/ui

Our configuration for hibernate (which is for our logger) is in src/main/resources/ui

Our tests are located in src/tests.

#### Running
1. Create new project in IntelliJ using the source files by choosing pom.xml 
 for Maven. This is important since it takes care of our dependencies.
2. A keystore file (.jks) must be setup and include the certificate that the
 server will present. We have included a keystore file in our submission so you
 can just use that one. If you would like to use your own keystore file, you will
 have to change the relevant fields in main.java.server.Install
3. Run main.java.server.Install to setup the databases and configuration files.
4. Run main.java.server.Server to start the server.
5. Access https://localhost:4567 in your browser.

#### Testing
Run test.TestAll (right click on class in project browser on the left). Please note that the test suite runs the install which will wipe all the databases.

#### External Dependencies
This code is best run using IntelliJ with Maven. We are using Java 1.8 for this project.

Below we have listed the dependencies we use. For more details about our dependencies, please refer to the pom.xml file, which is used by Maven.

- Our passwords, teams, and user information is stored inside a SQLite database. We use JDBC for storing in this database.
- Our logger also stores entries in an SQLite database. We use hibernate ORM to handle storing these log objects. We also included SQLDialect code from https://github.com/gwenn/sqlite-dialect
- Note that hibernate has a dependency on Log4j, so we also include it.
- We use the SparkJava framework for our server (http://sparkjava.com/).
- We use FindBugs for generating the XML having bug information.
- We use google's gson library to take care of json parsing and creation.
- We use the Sun JavaMail api for helping us send emails with verification codes.
- We use jetbrains annotations for internal development (they really help prevent null pointer bugs)

