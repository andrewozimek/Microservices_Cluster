package service_nodes;

import config.ConfigLoader;

public class ServiceNodeRunner {
    public static void main(String[] args) {
        // Load server settings from config file
        String serverHost = ConfigLoader.getServerHost();
        int serverUdpPort = ConfigLoader.getServerUdpPort();

        // Check if the user provided a service name in the terminal
        if (args.length < 1) {
            System.out.println("Error: No service specified.");
            System.out.println("Usage: java service_nodes.ServiceNodeRunner [base64|compression|csv|entropy|image]");
            return;
        }

        String selection = args[0].toLowerCase();
        String serviceName = "";
        int port = 0;

        // Logic to select only ONE service based on the command line argument
        switch (selection) {
            case "base64":
                serviceName = "Base64EncodeDecode";
                port = ConfigLoader.getServicePort("base64");
                break;
            case "compression":
                serviceName = "CompressionService";
                port = ConfigLoader.getServicePort("compression");
                break;
            case "csv":
                serviceName = "CSVStatsService";
                port = ConfigLoader.getServicePort("csv");
                break;
            case "entropy":
                serviceName = "FileEntropyAnalyzer";
                port = ConfigLoader.getServicePort("entropy");
                break;
            case "image":
                serviceName = "ImageTransform";
                port = ConfigLoader.getServicePort("image");
                break;
            default:
                System.out.println("Unknown service: " + selection);
                return;
        }

        // Start ONLY the selected service node in this JVM instance
        System.out.println("Starting " + serviceName + " on port " + port);
        ServiceNode node = new ServiceNode(serviceName, port, serverHost, serverUdpPort);
        Thread t = new Thread(node, "SN-" + serviceName);
        t.start();
    }
}