package service_nodes;

import java.io.*;
import java.util.zip.*;
import java.util.Base64;
import java.util.Scanner;

public class CompressionService {

    // Compresses string -> GZIP -> Base64 String
    public String compress(String str) throws IOException {
        if (str == null || str.length() == 0) return str;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            gzip.write(str.getBytes("UTF-8"));
        }
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }

    // Base64 String -> GZIP Decompress -> Original String
    public String decompress(String compressedBase64) throws IOException {
        if (compressedBase64 == null || compressedBase64.length() == 0) return compressedBase64;
        byte[] compressed = Base64.getDecoder().decode(compressedBase64);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        }
        return out.toString("UTF-8");
    }

    public static void main(String[] args) {
        CompressionService service = new CompressionService();
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("=== Compression Service Test Bench ===");
            System.out.println("Enter text to compress:");
            String input = scanner.nextLine();

            String compressed = service.compress(input);
            System.out.println("Compressed (Base64): " + compressed);
            System.out.println("Size reduction: " + input.length() + " chars -> " + compressed.length() + " chars");

            String decompressed = service.decompress(compressed);
            System.out.println("Decompressed Result: " + decompressed);
            System.out.println("Match: " + input.equals(decompressed));

        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            scanner.close();
        }
    }
}