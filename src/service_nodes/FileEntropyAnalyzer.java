package service_nodes;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

public class FileEntropyAnalyzer {

    // Calculate Shannon entropy of a byte array
    public double calculateEntropy(byte[] data) {
        if (data.length == 0) return 0.0;

        Map<Byte, Integer> freq = new HashMap<>();
        for (byte b : data) freq.merge(b, 1, Integer::sum);

        double entropy = 0.0;
        for (int count : freq.values()) {
            double prob = (double) count / data.length;
            entropy -= prob * (Math.log(prob) / Math.log(2));
        }
        return entropy;
    }

    // Interpret the entropy score
    public String interpret(double entropy) {
        if (entropy < 1.0) return "Highly repetitive / nearly empty";
        if (entropy < 3.5) return "Low entropy - plain text or structured data";
        if (entropy < 6.0) return "Medium entropy - mixed content";
        if (entropy < 7.5) return "High entropy - compressed or encrypted";
        return "Very high entropy - likely encrypted or random";
    }

    // Request format:
    // ANALYZE|<base64encodedfiledata>
    public String processRequest(String request) {
        try {
            String[] parts = request.split("\\|", 2);
            if (!parts[0].equalsIgnoreCase("ANALYZE"))
                return "ERROR: Unknown command '" + parts[0] + "'";

            byte[] data = Base64.getDecoder().decode(parts[1]);
            double entropy = calculateEntropy(data);

            return String.format("Entropy: %.4f bits/byte | %s", entropy, interpret(entropy));

        } catch (IllegalArgumentException e) {
            return "ERROR: Invalid Base64 input";
        }
    }

    public static void main(String[] args) {
        FileEntropyAnalyzer service = new FileEntropyAnalyzer();

        // Low entropy test - repetitive data
        byte[] lowEntropy = new byte[256];
        for (int i = 0; i < 256; i++) lowEntropy[i] = (byte) 'A';

        // High entropy test - random-ish data
        byte[] highEntropy = new byte[256];
        for (int i = 0; i < 256; i++) highEntropy[i] = (byte) i;

        String b64Low  = Base64.getEncoder().encodeToString(lowEntropy);
        String b64High = Base64.getEncoder().encodeToString(highEntropy);

        System.out.println("Low:  " + service.processRequest("ANALYZE|" + b64Low));
        System.out.println("High: " + service.processRequest("ANALYZE|" + b64High));
    }
}