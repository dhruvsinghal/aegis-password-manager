package main.java.server.util;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static main.java.util.Constants.MAX_VERIFICATION_TIME;

/**
 * Verification Code manager
 */
public class VerificationCodeManager {
    //Constants
    private static final String codeSpace = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static int codeLen = 10; //Length of the verification code

    //map from verification code to username
    private final ConcurrentHashMap<String, VerificationCode> verificationCodes;

    //Useful fields
    private final static SecureRandom random = new SecureRandom();


    /**
     * Constructor for session manager
     */
    public VerificationCodeManager() {
        verificationCodes = new ConcurrentHashMap<>();

        //Start the reaper thread for session ids
        new Thread(this::reaper).start();
    }

    /**
     * Reaper thread for removing verifications codes after timeout
     */
    private void reaper() {
        while (true) {
            for (Map.Entry<String, VerificationCode> entry : verificationCodes.entrySet()) {
                if (!isVerificationCodeValid(entry.getValue())) {
                    verificationCodes.remove(entry.getKey());
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
     * Generates a verification code that maps to the particular email
     *
     * @return the verification code as a string
     */
    public synchronized String generateVerificationCode(String email) {
        String code = generateVerificationCode();
        VerificationCode vc = new VerificationCode(code, email);
        verificationCodes.put(code, vc);
        return code;
    }

    /**
     * Adds in a verification code email pair. This should really only be used for testing.
     */
    public void addVerificationCode(String code, String email) {
        VerificationCode vc = new VerificationCode(code, email);
        verificationCodes.put(code, vc);
    }

    /**
     * Remove the code from the manager
     */
    public void removeCode(String code) {
        verificationCodes.remove(code);
    }

    /**
     * Returns the email associated with the given verification code.
     * If the verification code is invalid, returns empty optional.
     */
    public Optional<String> getEmail(String code) {
        return Optional.ofNullable(verificationCodes.get(code)).map(x -> x.email);
    }

    /**
     * Generates a verification code
     */
    private String generateVerificationCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(codeLen);
            for (int i = 0; i < codeLen; i++) {
                sb.append(codeSpace.charAt(random.nextInt(codeSpace.length())));
            }
            code = sb.toString();
        } while (verificationCodes.containsKey(code));

        return code;
    }

    /**
     * @return if the given session id is valid
     */
    private boolean isVerificationCodeValid(VerificationCode vCode) {
        return System.currentTimeMillis() - vCode.time < MAX_VERIFICATION_TIME;
    }

    private class VerificationCode {
        @NotNull
        private String code;

        @NotNull
        private String email;

        private long time;


        private VerificationCode(@NotNull String code, @NotNull String email) {
            this.time = System.currentTimeMillis();
            this.email = email;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VerificationCode that = (VerificationCode) o;

            if (!code.equals(that.code)) return false;
            return email.equals(that.email);

        }

        @Override
        public int hashCode() {
            int result = code.hashCode();
            result = 31 * result + email.hashCode();
            return result;
        }
    }
}
