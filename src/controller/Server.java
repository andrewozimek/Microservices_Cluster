package controller;

import java.io.*;
import java.net.*;
import java.util.Properties;

import client.Client;

public class Server {

    public static final int TCP_PORT;
    public static final int UDP_PORT;

    // get udp and tcp port from properties
    static{
        Properties props = new Properties();
        try(InputStream in = Client.class.getClassLoader().getResourceAsStream("config/cluster.properties")){
            // error handeling
            if(in == null){
                throw new RuntimeException("Could not find config/cluster.properties on classpath");
            }

            props.load(in);
            TCP_PORT = Integer.parseInt(props.getProperty("server.tcp.port"));
            UDP_PORT = Integer.parseInt(props.getProperty("server.udp.port"));
        }
        catch(Exception e){
            throw new RuntimeException("Failed to load cluster.properties", e);
        }
    }

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