package controller;

import java.io.*;
import java.net.*;
import config.ConfigLoader;

public class Server {

    public static void main(String[] args) throws IOException {

        System.out.println("Server starting...");
        
        int tcpPort = ConfigLoader.getServerTcpPort();
        int udpPort = ConfigLoader.getServerUdpPort();

        //Start UDP heartbeat listener thread here
        new Thread(new UDPListener(udpPort)).start();

        // TCP listener - main thread
        ServerSocket serverSocket = new ServerSocket(tcpPort);
        System.out.println("Listening for clients on port " + tcpPort);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Spawn a new thread for each client
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}