package main.java.server;

import main.java.database.AegisPasswordDatabase;
import main.java.model.AegisUser;
import main.java.util.Constants;

import java.util.Optional;

/**
 * This class is for helping directly edit the database
 */
public class DBEditor {
    public static AegisPasswordDatabase db;

    /**
     * Main method!
     */
    public static void main(String[] args) throws Exception {
        db = new AegisPasswordDatabase(Constants.DB_NAME);
        changePublicKey("ds793@cornell.edu", db); //TODO put email in
    }

    /**
     * 0s out the public key of the user with the given email in the database.
     * This is good for showing that our MAC is working
     */
    public static void changePublicKey(String email, AegisPasswordDatabase db) throws Exception {
        Optional<AegisUser> au = db.readUser(email);
        if(au.isPresent()){
            AegisUser user = au.get();
            String publicKey = user.userInfo.publicKey;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < publicKey.length(); i++){
                sb.append(" ");
            }

            AegisUser newUser = user.withUserInfo(user.userInfo.withPublicKey(sb.toString()));
            db.updateUser(newUser);
        } else {
            System.out.println("User was not found in the database. Email: " + email);
        }
    }
}
