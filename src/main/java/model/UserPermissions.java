package main.java.model;

/**
 * The permissions a user could have in a team
 */
public enum UserPermissions {
    READ(0),
    WRITE(1),
    ADMIN(2);

    public final int level;

    UserPermissions(int level) {
        this.level = level;
    }
}
