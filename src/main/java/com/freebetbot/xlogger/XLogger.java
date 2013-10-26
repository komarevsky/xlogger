/*
 * XLogger software is published under GPLv2 license
 *
 * Author : Siarhei Skavarodkin
 * email  : komarevsky (at) gmail (dot) com
 *
 */

package com.freebetbot.xlogger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * <h3>Logger.</h3>
 * <p>Creates(if not exists) directory {@link XLogger#LOGS_DIRECTORY_NAME} 
 * and writes logs into this dir.</p>
 * <hr>
 * Usage. Call the following methods to send logs:
 * <ul>
 * <li>{@link XLogger#sendFinest(java.lang.String)}
 * <li>{@link XLogger#sendFiner(java.lang.String)}
 * <li>{@link XLogger#sendFine(java.lang.String)}
 * <li>{@link XLogger#sendConfig(java.lang.String)}
 * <li>{@link XLogger#sendInfo(java.lang.String)}
 * <li>{@link XLogger#sendWarning(java.lang.String)}
 * <li>{@link XLogger#sendSevere(java.lang.Exception)}
 * </ul>
 * 
 * <p>Additionaly, there is {@link XLogger#exceptionToString(java.lang.Exception)}
 * method which can gives you String-representation of Exception</p>
 * <p>For changing log level, use
 * {@link XLogger#setLogLevel(java.util.logging.Level)}</p>
 * 
 * @author Siarhei Skavarodkin
 */
public class XLogger {

    public static final String DEFAULT_PATTERN = "x_logger.log";
    public static final String LOGS_DIRECTORY_NAME = "logs/";
    private static final String PACKAGE_NAME = XLogger.class.getPackage().getName();
    private static final int FILE_SIZE_LIMIT = 3 * 1024 * 1024; //3 MB
    private static final int FILE_COUNT_LIMIT = 2048;
    private static final Logger LOGGER = Logger.getLogger(XLogger.class.getName());
    
    // true, if setup method was called
    private static boolean initialized = false;

    
    /**
     * Makes all required settings for java.util.logging<br>
     * As filePattern {@link XLogger#DEFAULT_PATTERN} is used
     * @throws IOException
     */
    public static void setup() throws IOException {
        setup(DEFAULT_PATTERN);
    }
    
    /**
     * Makes all required settings for java.util.logging
     * @param filePattern pattern for files with logs
     * @throws IOException 
     */
    public static void setup(String filePattern) throws IOException {
        setup("", filePattern);
    }
    
    public static void setup(String dirName, String filePattern) throws IOException {
        String logDirName;
        if (dirName != null && !dirName.isEmpty()) {
            logDirName = dirName;
        } else {
            logDirName = LOGS_DIRECTORY_NAME;
        }
        
        //create directory if not exists
        File logDir = new File(logDirName);
        if (!logDir.exists()) {
            boolean isDirCreated = logDir.mkdir();
            if (!isDirCreated) {
                //unable to use specified dir. Writing to program work dir.
                logDirName = "";
            }
        } else if (!logDir.canWrite()) {
            //unable to use specified dir. Writing to program work dir.
            logDirName = "";
        }

        // Create Logger
        Logger logger = Logger.getLogger(PACKAGE_NAME);
        logger.setLevel(Level.FINEST);
        FileHandler fileTxt = new FileHandler(logDirName + filePattern,
                FILE_SIZE_LIMIT, FILE_COUNT_LIMIT, false);

        // Create txt Formatter
        SimpleFormatter formatterTxt = new SimpleFormatter();
        fileTxt.setFormatter(formatterTxt);
        logger.addHandler(fileTxt);
        
        initialized = true;
    }
    
    public static void setLogLevel(Level level) {
        if (level != null) {
            Logger logger = Logger.getLogger(PACKAGE_NAME);
            logger.setLevel(level);
            sendInfo("Log level is changed to " + level.getName());
        }
    }

    public static void sendFinest(String s) {
        sendLog(s, Level.FINEST);
    }
    
    public static void sendFiner(String s) {
        sendLog(s, Level.FINER);
    }

    public static void sendFine(String s) {
        sendLog(s, Level.FINE);
    }

    public static void sendConfig(String s) {
        sendLog(s, Level.CONFIG);
    }
    
    public static void sendInfo(String s) {
        sendLog(s, Level.INFO);
    }

    public static void sendWarning(String s) {
        sendLog(s, Level.WARNING);
    }

    public static void sendSevere(String s) {
        sendLog(s, Level.SEVERE);
    }
    
    public static void sendSevere(Exception ex) {
        String s = exceptionToString(ex);
        sendLog(s, Level.SEVERE);
    }
    
    public static String exceptionToString(Exception ex) {
        if (ex == null) {
            return "null";
        }
        
        String result = 
                ex.getClass().getCanonicalName() + "  "
                + ex.getMessage() + ":\n";
        
        StackTraceElement[] stackTrace = ex.getStackTrace();
        for (StackTraceElement el : stackTrace) {
            result += el.getClassName() + "."
                    + el.getMethodName() + ":" 
                    + el.getLineNumber()+ "\n";
        }
        
        return result;
    }
    
    private static void sendLog(String s, Level level) {
        //0 -getStackTrace; 1 -sendLog; 2 -sendXXX; 3 -real caller
        final int CALLER_NUMBER_IN_STACK = 3;
        
        if (!initialized) {
            try {
                setup();
            } catch (Exception ex) {
                System.err.println(exceptionToString(ex));
            }
        }
        
        if (LOGGER.isLoggable(level)) {
            
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            
            String msg; //what to log
            
            // add additional info to the log message, if possible
            if (stackTraceElements.length >= CALLER_NUMBER_IN_STACK + 1) {
                //get calling class.method:line + msg
                StackTraceElement el = stackTraceElements[CALLER_NUMBER_IN_STACK];
                msg = el.getClassName() + "."
                    + el.getMethodName() + ":" 
                    + el.getLineNumber()+ "\n"
                    + s + "\n";
            } else {
                msg = s + "\n";
            }
            
            LOGGER.log(level, msg);
        }
    }
        
}
