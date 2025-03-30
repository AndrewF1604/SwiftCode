import java.io.*;
import java.util.*;

public class ConfigReader {
    private Properties properties = new Properties();

    public ConfigReader(String filePath) {
        try (InputStream input = new FileInputStream(filePath)) {
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key, "").trim();
    }
}
