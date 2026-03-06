package service_nodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class ServiceNode implements Runnable{

    private final String serviceName;
    private final int tcpPort;
    private final String serverHost;
    private final int serverUdpPort;

    private volatile boolean running = true;

    public ServiceNode(String serviceName, int tcpPort, String serverHost, int serverUdpPort){
        this.serviceName = serviceName;
        this.tcpPort = tcpPort;
        this.serverHost = serverHost;
        this.serverUdpPort = serverUdpPort;
    }

    public static void main(String[] args){
        // usage error handeling
        if (args.length < 2) {
            System.out.println("Usage: java src.service_nodes.ServiceNode <serviceName> <tcpPort> [serverHost=127.0.0.1] [serverUdpPort=5001]");
            System.exit(1);
        }

        String serviceName = args[0];
        int tcpPort = Integer.parseInt(args[1]);
        String serverHost = (args.length >= 3) ? args[2] : "127.0.0.1";
        int serverUdpPort = (args.length >= 4) ? Integer.parseInt(args[3]) : 5051;

        ServiceNode node = new ServiceNode(serviceName, tcpPort, serverHost, serverUdpPort);
        node.run();
    }

    @Override
    public void run() {
        // start heartbeat thread
        Thread heartbeatThread = new Thread(new HeartbeatTask(), "Heartbeat-" +serviceName);
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();

        runTcpServer();
    }
    
    private void runTcpServer(){
        System.out.println(serviceName + " starting TCP server on port " + tcpPort);
        try (ServerSocket serverSocket = new ServerSocket(tcpPort)) {
            while (running) {
                Socket s = serverSocket.accept();
                // handle one request per connection 
                handleOneRequest(s);
            }
        } catch (IOException e) {
            System.err.println(serviceName + " TCP server error: " + e.getMessage());
        }
    }

    private void handleOneRequest(Socket s) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line = in.readLine(); // one-line request
            if (line == null) {
                out.println("ERROR|Empty request");
                return;
            }

            String response = processRequest(line.trim());
            out.println(response);

        } catch (Exception e) {
            try {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                out.println("ERROR|" + e.getClass().getSimpleName() + ":" + e.getMessage());
            } catch (Exception ignored) { }
        } finally {
            try { s.close(); } catch (IOException ignored) {}
        }
    }

    private String processRequest(String requestLine) throws Exception {
        String[] parts = requestLine.split("\\|", 2);
        String command = parts[0].toUpperCase();
        String data = (parts.length > 1) ? parts[1] : "";

        switch (serviceName) {
            case "Base64EncodeDecode":
                return handleBase64(command, data);

            case "CompressionService":
                return handleCompression(command, data);

            case "CSVStatsService":
                return handleCsvStats(command, data);

            case "FileEntropyAnalyzer":
                return handleEntropy(command, data);

            case "ImageTransform":
                return handleImage(command, data);

            default:
                return "ERROR|Unknown serviceName: " + serviceName;
        }
    }

    private String handleBase64(String command, String data) {
        Base64EncodeDecode svc = new Base64EncodeDecode();
        if (command.equals("ENCODE")) {
            return "OK|" + svc.encode(data);
        }
        if (command.equals("DECODE")) {
            String result = svc.decode(data);
            if (result.startsWith("Error:")) {
                return "ERROR|" + result;
            }
            return "OK|" + result;
        }
        return "ERROR|Base64 commands: ENCODE|text or DECODE|base64";
    }

    private String handleCompression(String command, String data) throws IOException {
        CompressionService svc = new CompressionService();
        if (command.equals("COMPRESS")) {
            return "OK|" + svc.compress(data);
        }
        if (command.equals("DECOMPRESS")) {
            return "OK|" + svc.decompress(data);
        }
        return "ERROR|Compression commands: COMPRESS|text or DECOMPRESS|base64(gzipBytes)";
    }

    private String handleCsvStats(String command, String data) {
        if (!command.equals("STATS")) {
            return "ERROR|CSVStats commands: STATS|commaSeparatedNumbers";
        }
        CSVStatsService svc = new CSVStatsService();
        String result = svc.processCSV(data);
        if (result.startsWith("Error:")) {
            return "ERROR|" + result;
        }
        return "OK|" + result;
    }

    private String handleEntropy(String command, String data) {
        if (!command.equals("ANALYZE")) {
            return "ERROR|Entropy commands: ANALYZE|base64EncodedFileData";
        }
        FileEntropyAnalyzer svc = new FileEntropyAnalyzer();
        String result = svc.processRequest("ANALYZE|" + data);
        if (result.startsWith("ERROR:")) {
            return "ERROR|" + result;
        }
        return "OK|" + result;
    }

    private String handleImage(String command, String data) {
        if (command.equals("PING")) return "OK|PONG";
        ImageTransform svc = new ImageTransform();
        String fullRequest = data == null || data.isEmpty() ? command : command + "|" + data;
        String result = svc.processRequest(fullRequest);
        if (result.startsWith("ERROR:")) {
            return "ERROR|" + result;
        }
        return "OK|" + result;
    }



    // heartbeat task
    private class HeartbeatTask implements Runnable {
        private final Random rand = new Random();

        @Override
        public void run() {
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress serverAddr = InetAddress.getByName(serverHost);

                while (running) {
                    String msg = "HEARTBEAT:" + serviceName + ":" + tcpPort;
                    byte[] buf = msg.getBytes(StandardCharsets.UTF_8);

                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, serverUdpPort);
                    socket.send(packet);

                    // sleep random 15–30s
                    int sleepMs = 15_000 + rand.nextInt(15_001);
                    Thread.sleep(sleepMs);
                }

            } catch (Exception e) {
                System.err.println(serviceName + " heartbeat error: " + e.getMessage());
            }
        }
    }

}
