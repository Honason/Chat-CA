package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Lars Mortensen
 */
public class Utils {

    public static Properties initProperties(String propertyFile) {
        Properties properties = new Properties();
        try (InputStream is = new FileInputStream(propertyFile)) {
            properties.load(is);
        } catch (IOException ex) {
            System.out.println(String.format("Could not locate the %1$s file.", propertyFile));
            return null;
        }
        return properties;
    }

    public static void setLogFile(String logFile, String className) {
        try {
            Logger logger = Logger.getLogger(className);
            FileHandler fileTxt = new FileHandler(logFile);
            java.util.logging.Formatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Call this to delete the ".lck" file
     *
     * @param logger
     */
    public static void closeLogger(String logger) {
        for (Handler h : Logger.getLogger(logger).getHandlers()) {
            System.out.println("Closing logger");
            h.close();
        }
    }

    public static Logger getLogger(String logFile, String className) {
        Logger logger;
        try {
            logger = Logger.getLogger(className);
            FileHandler fileTxt = new FileHandler(logFile);
            java.util.logging.Formatter formatterTxt = new SimpleFormatter();
            fileTxt.setFormatter(formatterTxt);
            logger.addHandler(fileTxt);
        } catch (IOException | SecurityException ex) {
            Logger.getLogger(className).log(Level.SEVERE, null, ex);
            return null;
        }
        return logger;
    }

}
