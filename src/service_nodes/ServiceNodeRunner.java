package service_nodes;

public class ServiceNodeRunner {
    public static void main(String[] args) {
        // Default server settings
        String serverHost = "127.0.0.1";
        int serverUdpPort = 5051;

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
                port = 6001;
                break;
            case "compression":
                serviceName = "CompressionService";
                port = 6002;
                break;
            case "csv":
                serviceName = "CSVStatsService";
                port = 6003;
                break;
            case "entropy":
                serviceName = "FileEntropyAnalyzer";
                port = 6004;
                break;
            case "image":
                serviceName = "ImageTransform";
                port = 6005;
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