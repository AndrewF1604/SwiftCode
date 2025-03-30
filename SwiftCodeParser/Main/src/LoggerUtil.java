import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {
    private static Logger logger = Logger.getLogger("SwiftLogger");

    public static void setupLogger(String logFilePath) {
        try {
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            System.err.println("Failed to set up logger: " + e.getMessage());
        }
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message) {
        logger.severe(message);
    }
}
