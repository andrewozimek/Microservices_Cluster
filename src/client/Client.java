package client;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Base64;
import java.util.Scanner;
import config.ConfigLoader;

public class Client {

    // Stores paths and commands so the result handler can save output files next to input
    private static String lastImagePath = null;
    private static String lastFilePath = null;
    private static String lastCmd = null;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        String serverHost = ConfigLoader.getServerHost();
        int serverTcpPort = ConfigLoader.getServerTcpPort();

        System.out.println("=== QU Microservices Cluster Client ===");
        System.out.println("Connecting to server at " + serverHost + ":" + serverTcpPort);

        while (true) {
            try (Socket socket = new Socket(serverHost, serverTcpPort);
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
                    continue;
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
                if (result == null) {
                    System.out.println("(no response)");
                } else if (result.startsWith("ERROR")) {
                    System.out.println(result);
                } else if (selectedService.equals("ImageTransform")) {
                    // decode Base64 and save to file - strip OK| prefix if present
                    String base64Image = result.startsWith("OK|") ? result.substring(3) : result;
                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                    // Save output image to same folder as input image
                    String parentDir = Paths.get(lastImagePath).getParent().toString();
                    String outputPath = parentDir + "\\output.png";
                    Files.write(Paths.get(outputPath), imageBytes);
                    System.out.println("Image saved to: " + outputPath);
                } else if (selectedService.equals("Base64EncodeDecode")) {
                    String data = result.startsWith("OK|") ? result.substring(3) : result;
                    String parentDir = Paths.get(lastFilePath).getParent().toString();
                    if (lastCmd.equals("ENCODE")) {
                        // Save encoded text to a .txt file
                        String outputPath = parentDir + "\\encoded_output.txt";
                        Files.write(Paths.get(outputPath), data.getBytes());
                        System.out.println("Encoded file saved to: " + outputPath);
                    } else {
                        // Save decoded bytes back to a file
                        byte[] decoded = Base64.getDecoder().decode(data);
                        String outputPath = parentDir + "\\decoded_output.bin";
                        Files.write(Paths.get(outputPath), decoded);
                        System.out.println("Decoded file saved to: " + outputPath);
                    }
                } else if (selectedService.equals("CompressionService")) {
                    String data = result.startsWith("OK|") ? result.substring(3) : result;
                    String parentDir = Paths.get(lastFilePath).getParent().toString();
                    if (lastCmd.equals("COMPRESS")) {
                        // Decode Base64 back to actual compressed bytes and save as .gz
                        byte[] compressedBytes = Base64.getDecoder().decode(data);
                        String outputPath = parentDir + "\\compressed_output.gz";
                        Files.write(Paths.get(outputPath), compressedBytes);
                        System.out.println("Compressed file saved to: " + outputPath);
                    } else {
                        // Decode Base64 back to original bytes and save as .bin
                        byte[] decoded = Base64.getDecoder().decode(data);
                        String outputPath = parentDir + "\\decompressed_output.bin";
                        Files.write(Paths.get(outputPath), decoded);
                        System.out.println("Decompressed file saved to: " + outputPath);
                    }
                } else if (selectedService.equals("CSVStatsService")) {
                    String data = result.startsWith("OK|") ? result.substring(3) : result;
                    // Format nicely: each column on its own line
                    String formatted = data.replace(" || ", "\n").replace("||", "").trim();
                    // Save to file next to input
                    String parentDir = Paths.get(lastFilePath).getParent().toString();
                    String outputPath = parentDir + "\\csv_results.txt";
                    Files.write(Paths.get(outputPath), formatted.getBytes());
                    System.out.println("CSV stats saved to: " + outputPath);
                    System.out.println(formatted);
                } else {
                    // FileEntropyAnalyzer — print to terminal
                    System.out.println(result);
                }
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
                System.out.print("Enter file path: ");
                String path = scanner.nextLine().trim();
                lastFilePath = path;
                lastCmd = cmd;
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    // ENCODE: read raw bytes and encode them
                    // DECODE: file contains Base64 text, read as string
                    String data = cmd.equals("ENCODE")
                            ? Base64.getEncoder().encodeToString(bytes)
                            : new String(bytes).trim();
                    return service + "|" + cmd + "|" + data;
                } catch (IOException e) {
                    System.out.println("Could not read file: " + e.getMessage());
                    return null;
                }
            }

            case "CompressionService": {
                System.out.println("Commands: COMPRESS or DECOMPRESS");
                System.out.print("Command: ");
                String cmd = scanner.nextLine().trim().toUpperCase();
                System.out.print("Enter file path: ");
                String path = scanner.nextLine().trim();
                lastFilePath = path;
                lastCmd = cmd;
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    // Both COMPRESS and DECOMPRESS: Base64 encode raw bytes before sending
                    String data = Base64.getEncoder().encodeToString(bytes);
                    return service + "|" + cmd + "|" + data;
                } catch (IOException e) {
                    System.out.println("Could not read file: " + e.getMessage());
                    return null;
                }
            }

            case "CSVStatsService": {
                System.out.print("Enter CSV file path: ");
                String path = scanner.nextLine().trim();
                lastFilePath = path;
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    String data = Base64.getEncoder().encodeToString(bytes);
                    return service + "|STATS|" + data;
                } catch (IOException e) {
                    System.out.println("Could not read file: " + e.getMessage());
                    return null;
                }
            }

            case "FileEntropyAnalyzer": {
                System.out.print("Enter file path: ");
                String path = scanner.nextLine().trim();
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    String data = Base64.getEncoder().encodeToString(bytes);
                    return service + "|ANALYZE|" + data;
                } catch (IOException e) {
                    System.out.println("Could not read file: " + e.getMessage());
                    return null;
                }
            }

            case "ImageTransform": {
                System.out.println("Commands: RESIZE, THUMBNAIL, ROTATE, GRAYSCALE");
                System.out.print("Command: ");
                String cmd = scanner.nextLine().trim().toUpperCase();
                System.out.print("Enter image file path: ");
                String path = scanner.nextLine().trim();
                lastImagePath = path; // store so main can save output next to it
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(path));
                    String img = Base64.getEncoder().encodeToString(bytes);
                    switch (cmd) {
                        case "RESIZE": {
                            System.out.print("Width: ");
                            String w = scanner.nextLine().trim();
                            System.out.print("Height: ");
                            String h = scanner.nextLine().trim();
                            return service + "|RESIZE|" + w + "|" + h + "|" + img;
                        }
                        case "ROTATE": {
                            System.out.print("Degrees (90/180/270): ");
                            String deg = scanner.nextLine().trim();
                            return service + "|ROTATE|" + deg + "|" + img;
                        }
                        case "THUMBNAIL":
                        case "GRAYSCALE": {
                            return service + "|" + cmd + "|" + img;
                        }
                        default:
                            System.out.println("Unknown image command.");
                            return null;
                    }
                } catch (IOException e) {
                    System.out.println("Could not read file: " + e.getMessage());
                    return null;
                }
            }

            default:
                System.out.println("No input handler for service: " + service);
                return null;
        }
    }
}
