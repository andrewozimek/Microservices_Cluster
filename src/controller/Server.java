package controller;

import java.io.*;
import java.net.*;

public class Server {

    public static final int TCP_PORT = 5000;
    public static final int UDP_PORT = 5001;

    public static void main(String[] args) throws IOException {

        System.out.println("Server starting...");

        //Start UDP heartbeat listener thread here
        new Thread(new UDPListener()).start();

        // TCP listener - main thread
        ServerSocket serverSocket = new ServerSocket(TCP_PORT);
        System.out.println("Listening for clients on port " + TCP_PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // Spawn a new thread for each client
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
}