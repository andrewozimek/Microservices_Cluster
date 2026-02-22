package src.service_nodes;

import java.util.*;

public class CSVStatsService {

    public String processCSV(String data) {
        try {
            // Split by comma and clean up whitespace
            String[] parts = data.split(",");
            List<Double> numbers = new ArrayList<>();
            
            for (String s : parts) {
                String trimmed = s.trim();
                if (!trimmed.isEmpty()) {
                    numbers.add(Double.parseDouble(trimmed));
                }
            }

            if (numbers.isEmpty()) return "Error: No valid numbers found.";

            // Basic Stats
            double sum = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;

            for (double n : numbers) {
                sum += n;
                if (n < min) min = n;
                if (n > max) max = n;
            }

            double mean = sum / numbers.size();

            // Median Logic (requires sorting)
            Collections.sort(numbers);
            double median;
            int size = numbers.size();
            if (size % 2 == 0) {
                median = (numbers.get(size / 2) + numbers.get(size / 2 - 1)) / 2.0;
            } else {
                median = numbers.get(size / 2);
            }

            // Standard Deviation
            double varianceSum = 0;
            for (double n : numbers) {
                varianceSum += Math.pow(n - mean, 2);
            }
            double stdDev = Math.sqrt(varianceSum / size);

            return String.format("Results -> Mean: %.2f, Median: %.2f, StdDev: %.2f, Min: %.2f, Max: %.2f", 
                                 mean, median, stdDev, min, max);

        } catch (NumberFormatException e) {
            return "Error: Input contains non-numeric values.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // Main method for manual testing
    public static void main(String[] args) {
        CSVStatsService service = new CSVStatsService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CSV Stats Service Test Bench ===");
        System.out.println("Enter numbers separated by commas (e.g., 10, 20, 30.5, 40):");
        
        if (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            String result = service.processCSV(input);
            System.out.println(result);
        }
        
        scanner.close();
    }
}