package service_nodes;

public class ServiceNodeRunner {
    public static void main(String[] args) {
        String serverHost = "127.0.0.1";
        int serverUdpPort = 5001;

        start("Base64EncodeDecode", 6001, serverHost, serverUdpPort);
        start("CompressionService", 6002, serverHost, serverUdpPort);
        start("CSVStatsService", 6003, serverHost, serverUdpPort);
        start("FileEntropyAnalyzer", 6004, serverHost, serverUdpPort);
        start("ImageTransform", 6005, serverHost, serverUdpPort);

        System.out.println("All service nodes started. (Threads in one JVM)");
    }

    private static void start(String name, int port, String host, int udpPort) {
        ServiceNode node = new ServiceNode(name, port, host, udpPort);
        Thread t = new Thread(node, "SN-" + name);
        t.start();
    }
}
