package controller;

import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

public class UDPListener implements Runnable {

    private final int udpPort;
    
    // aliveNodes: serviceName -> "ip:port"
    public static final ConcurrentHashMap<String, String> aliveNodes = new ConcurrentHashMap<>();

    // lastHeartbeat: serviceName -> timestamp of last heartbeat
    public static final ConcurrentHashMap<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    // timeout time is 120 seconds
    private static final long TIMEOUT_MS = 120000;

    /**
     * Constructor to create UDPListener with specified port
     * @param udpPort the port to listen for UDP heartbeats
     */
    public UDPListener(int udpPort) {
        this.udpPort = udpPort;
    }


    @Override
    public void run() {
        // start cleanup thread
        Thread cleanupThread = new Thread(() ->{
            while(true){
                try{
                    long now = System.currentTimeMillis();
                    for(Map.Entry<String, Long> entry : lastHeartbeat.entrySet()){
                        String serviceName = entry.getKey();
                        long lastSeen = entry.getValue();
                        // if dead for more than 120 seconds remove form aliveNodes
                        if (now - lastSeen > TIMEOUT_MS) {
                            aliveNodes.remove(serviceName);
                            lastHeartbeat.remove(serviceName);
                            System.out.println("Service node expired: " + serviceName);
                        }
                    }
                    // check every 5 seconds
                    Thread.sleep(5000);
                } catch(InterruptedException e){
                    System.err.println("Cleanup thread interrupted: " + e.getMessage());
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.start();

        try (DatagramSocket udpSocket = new DatagramSocket(udpPort)) {
            System.out.println("UDP listener started on port " + udpPort);

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength()).trim();
                String senderIP = packet.getAddress().getHostAddress();

                //format: HEARTBEAT:serviceName:tcpPort
                if (message.startsWith("HEARTBEAT:")) {
                    String[] parts = message.split(":");
                    if (parts.length == 3) {
                        String serviceName = parts[1];
                        String tcpPort = parts[2];

                        // Store: serviceName -> ip:port
                        aliveNodes.put(serviceName, senderIP + ":" + tcpPort);
                        // update last seen time
                        lastHeartbeat.put(serviceName, System.currentTimeMillis());
                        System.out.println("SN registered: " + serviceName + " @ " + senderIP + ":" + tcpPort);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("UDPListener error: " + e.getMessage());
        }
    }
}