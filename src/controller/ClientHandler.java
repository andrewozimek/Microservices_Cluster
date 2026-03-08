package controller;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.stream.Collectors;

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
            //Send available services to client using aliveNodes from UDP listener
            Set<String> services = UDPListener.aliveNodes.keySet();
            if(services.isEmpty()){
                out.println("ERROR:No services available");
                return;
            }
            String serviceList = services.stream().sorted().collect(Collectors.joining(","));
            out.println("SERVICES:" + serviceList);

            //Read client's service request (format: serviceName|payload)
            String request = in.readLine();
            System.out.println("Client requested: " + request);

            //Validate the request
            if (request == null || request.isEmpty()) {
                out.println("ERROR:No service requested");
                return;
            }

            // split service name and rest of info
            String[] parts = request.split("\\|", 2);
            if(parts.length < 2){
                out.println("ERROR:Invalid request format. Use serviceName|payload");
                return;
            }
            String serviceName = parts[0].trim();
            String payload = parts[1].trim();

            // look up service node from aliveNodes
            String addr = UDPListener.aliveNodes.get(serviceName);
            if(addr == null){
                out.println("ERROR:Service not available: " + serviceName);
                return;
            }
            // addr format should be host:port
            String[] addrParts = addr.split(":");
            if(addrParts.length != 2){
                out.println("ERROR:Invalid service address for " + serviceName);
                return;
            }
            String host = addrParts[0];
            int port = Integer.parseInt(addrParts[1]);

            //forward to service node over tcp
            //out.println("ACK:" + request + " received, processing...");
            try(
                Socket snSocket = new Socket(host,port);
                PrintWriter snOut = new PrintWriter(snSocket.getOutputStream(), true);
                BufferedReader snIn = new BufferedReader(new InputStreamReader(snSocket.getInputStream()));
            ){
                snOut.println(payload);
                String response = snIn.readLine();
                if(response == null){
                    out.println("ERROR:Service node did not respond");
                }
                else{
                    out.println(response);
                }
            }
            catch(Exception e){
                out.println("ERROR:Failed to contact service node: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("ClientHandler error: " + e.getMessage());
        } finally {
            try { clientSocket.close(); } catch (IOException ignored) {}
        }
    }
}