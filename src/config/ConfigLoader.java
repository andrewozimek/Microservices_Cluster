package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for loading configuration values from config.properties file.
 * This class provides static methods to read server and service configuration.
 */
public class ConfigLoader {

    private static Properties properties;
    private static final String CONFIG_FILE = "config.properties";

    static {
        properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.err.println("WARNING: config.properties file not found in classpath. Using defaults.");
            } else {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("WARNING: Could not load config.properties: " + e.getMessage());
        }
    }

    /**
     * Get the server host address
     * Checks environment variable SERVER_HOST first, then falls back to config.properties
     * @return the server host (default: 127.0.0.1)
     */
    public static String getServerHost() {
        // Check environment variable first
        String envHost = System.getenv("SERVER_HOST");
        if (envHost != null && !envHost.isEmpty()) {
            System.out.println("Using SERVER_HOST from environment: " + envHost);
            return envHost;
        }
        
        // Fall back to config.properties
        return properties.getProperty("server.host", "127.0.0.1");
    }

    /**
     * Get the server TCP port
     * @return the TCP port (default: 5050)
     */
    public static int getServerTcpPort() {
        String port = properties.getProperty("server.tcp.port", "5050");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("WARNING: Invalid server.tcp.port value: " + port + ". Using default 5050");
            return 5050;
        }
    }

    /**
     * Get the server UDP port (for heartbeat messages)
     * @return the UDP port (default: 5051)
     */
    public static int getServerUdpPort() {
        String port = properties.getProperty("server.udp.port", "5051");
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            System.err.println("WARNING: Invalid server.udp.port value: " + port + ". Using default 5051");
            return 5051;
        }
    }

    /**
     * Get the TCP port for a specific service
     * @param serviceName the service name (e.g., "base64", "compression", "csv", "entropy", "image")
     * @return the TCP port for the service
     */
    public static int getServicePort(String serviceName) {
        String key = "service." + serviceName.toLowerCase() + ".port";
        String port = properties.getProperty(key);
        
        if (port == null) {
            throw new IllegalArgumentException("Port configuration not found for service: " + serviceName);
        }
        
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port value for service " + serviceName + ": " + port);
        }
    }

    /**
     * Reload properties from the config file
     * Useful for testing or dynamic configuration updates
     */
    public static void reload() {
        properties = new Properties();
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            System.err.println("ERROR: Could not reload config.properties: " + e.getMessage());
        }
    }
}
