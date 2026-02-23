package controller;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        System.out.println("Handling client: " + clientSocket.getInetAddress());

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            //Send available services to client
            out.println("SERVICES:Base64EncodeDecode,CompressionService,CSVStats,FileEntropy,ImageTransform");

            //Read client's service request
            String request = in.readLine();
            System.out.println("Client requested: " + request);

            //Validate the request
            if (request == null || request.isEmpty()) {
                out.println("ERROR:No service requested");
                return;
            }

            //TODO - forward to service node (coming later)
            out.println("ACK:" + request + " received, processing...");

        } catch (IOException e) {
            System.err.println("ClientHandler error: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}