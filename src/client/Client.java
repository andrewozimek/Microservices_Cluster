package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_TCP_PORT = 5050;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== QU Microservices Cluster Client ===");
        System.out.println("Connecting to server at " + SERVER_HOST + ":" + SERVER_TCP_PORT);

        while (true) {
            try (Socket socket = new Socket(SERVER_HOST, SERVER_TCP_PORT);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String serviceListLine = in.readLine();
                if (serviceListLine == null) {
                    System.out.println("Server closed connection.");
                    break;
                }

                if (!serviceListLine.startsWith("SERVICES:")) {
                    System.out.println("Server Message: " + serviceListLine);
                    System.out.println("Press Enter to try again...");
                    scanner.nextLine();
                    continue; // Loops back instead of exiting
                }

                String[] services = serviceListLine.substring("SERVICES:".length()).split(",");

                System.out.println("\nAvailable services:");
                for (int i = 0; i < services.length; i++) {
                    System.out.println("  [" + (i + 1) + "] " + services[i]);
                }
                System.out.println("  [0] Quit");
                System.out.print("Select a service: ");

                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    System.out.println("Goodbye.");
                    break;
                }
                if (choice < 1 || choice > services.length) {
                    System.out.println("Invalid choice.");
                    continue;
                }

                String selectedService = services[choice - 1].trim();

                String requestLine = buildRequest(selectedService, scanner);
                if (requestLine == null) {
                    System.out.println("Cancelled.");
                    continue;
                }

                out.println(requestLine);

                String result = in.readLine();
                System.out.println("\n--- Result ---");
                System.out.println(result != null ? result : "(no response)");
                System.out.println("--------------\n");

            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (ConnectException e) {
                System.out.println("Could not connect to server: " + e.getMessage());
                break;
            }
        }

        scanner.close();
    }

    private static String buildRequest(String service, Scanner scanner) {
        switch (service) {
            case "Base64EncodeDecode": {
                System.out.println("Commands: ENCODE or DECODE");
                System.out.print("Command: ");
                String cmd = scanner.nextLine().trim().toUpperCase();
                System.out.print("Input text: ");
                String data = scanner.nextLine().trim();
                return service + "|" + cmd + "|" + data;
            }
            case "CompressionService": {
                System.out.println("Commands: COMPRESS or DECOMPRESS");
                System.out.print("Command: ");
                String cmd = scanner.nextLine().trim().toUpperCase();
                System.out.print("Input text: ");
                String data = scanner.nextLine().trim();
                return service + "|" + cmd + "|" + data;
            }
            case "CSVStatsService": {
                System.out.print("Enter numbers separated by commas: ");
                String data = scanner.nextLine().trim();
                return service + "|STATS|" + data;
            }
            case "FileEntropyAnalyzer": {
                System.out.print("Enter Base64-encoded file data: ");
                String data = scanner.nextLine().trim();
                return service + "|ANALYZE|" + data;
            }
            case "ImageTransform": {
                System.out.println("Commands: RESIZE, THUMBNAIL, ROTATE, GRAYSCALE");
                System.out.print("Command: ");
                String cmd = scanner.nextLine().trim().toUpperCase();
                switch (cmd) {
                    case "RESIZE": {
                        System.out.print("Width: ");
                        String w = scanner.nextLine().trim();
                        System.out.print("Height: ");
                        String h = scanner.nextLine().trim();
                        System.out.print("Base64 image: ");
                        String img = scanner.nextLine().trim();
                        return service + "|RESIZE|" + w + "|" + h + "|" + img;
                    }
                    case "ROTATE": {
                        System.out.print("Degrees (90/180/270): ");
                        String deg = scanner.nextLine().trim();
                        System.out.print("Base64 image: ");
                        String img = scanner.nextLine().trim();
                        return service + "|ROTATE|" + deg + "|" + img;
                    }
                    case "THUMBNAIL":
                    case "GRAYSCALE": {
                        System.out.print("Base64 image: ");
                        String img = scanner.nextLine().trim();
                        return service + "|" + cmd + "|" + img;
                    }
                    default:
                        System.out.println("Unknown image command.");
                        return null;
                }
            }
            default:
                System.out.println("No input handler for service: " + service);
                return null;
        }
    }
}
