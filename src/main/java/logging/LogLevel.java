package main.java.logging;

/**
 * The various valid logging levels
 */
public enum LogLevel {
    DEBUG,                        //for admin purposes
    INFO,                         //for normal mutations
    WARNING,                      //for unauthorized access
    ERROR,                        //for general errors
    AUTH_ERROR                    //Auth error generally means someone failed to authenticate. This is bad
}
