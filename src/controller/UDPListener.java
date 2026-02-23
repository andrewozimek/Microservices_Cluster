package controller;

import java.net.*;
import java.util.concurrent.*;

public class UDPListener implements Runnable {

    public static final ConcurrentHashMap<String, String> aliveNodes = new ConcurrentHashMap<>();

    @Override
    public void run() {
        try (DatagramSocket udpSocket = new DatagramSocket(Server.UDP_PORT)) {
            System.out.println("UDP listener started on port " + Server.UDP_PORT);

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
                        System.out.println("SN registered: " + serviceName + " @ " + senderIP + ":" + tcpPort);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("UDPListener error: " + e.getMessage());
        }
    }
}