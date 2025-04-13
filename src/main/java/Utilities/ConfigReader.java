package Utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader extends DriverFactory{
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigReader.class.getClassLoader().getResourceAsStream("LambdaTestHackathon.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties");
            }
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load properties file");
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}
