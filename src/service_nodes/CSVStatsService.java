package service_nodes;

import java.util.*;

public class CSVStatsService {

    public String processCSV(String data) {
        try {
            // Base64 decode the incoming CSV data
            byte[] decoded = java.util.Base64.getDecoder().decode(data);
            data = new String(decoded).trim();

            String[] rows = data.split("\\r?\\n");
            if (rows.length == 0) return "Empty CSV.";

            // Parse header row
            String[] headers = rows[0].split(",");
            int numCols = headers.length;

            // Collect values per column
            List<List<Double>> columnData = new ArrayList<>();
            for (int i = 0; i < numCols; i++) {
                columnData.add(new ArrayList<>());
            }

            // Parse data rows
            for (int r = 1; r < rows.length; r++) {
                String row = rows[r].trim();
                if (row.isEmpty()) continue;
                String[] cells = row.split(",");
                for (int c = 0; c < cells.length && c < numCols; c++) {
                    try {
                        double val = Double.parseDouble(cells[c].trim());
                        columnData.get(c).add(val);
                    } catch (NumberFormatException e) {
                        // skip text values (names, URLs, IDs, etc.)
                    }
                }
            }

            // Build result for numeric columns only (single line, | separated)
            StringBuilder sb = new StringBuilder("Results: ");
            boolean anyNumeric = false;

            for (int c = 0; c < numCols; c++) {
                List<Double> vals = columnData.get(c);
                if (vals.isEmpty()) continue; // skip text-only columns

                anyNumeric = true;
                String colName = headers[c].trim();

                double sum = 0, min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
                for (double v : vals) {
                    sum += v;
                    if (v < min) min = v;
                    if (v > max) max = v;
                }
                double mean = sum / vals.size();

                Collections.sort(vals);
                double median;
                int sz = vals.size();
                if (sz % 2 == 0) {
                    median = (vals.get(sz / 2) + vals.get(sz / 2 - 1)) / 2.0;
                } else {
                    median = vals.get(sz / 2);
                }

                double varianceSum = 0;
                for (double v : vals) varianceSum += Math.pow(v - mean, 2);
                double stdDev = Math.sqrt(varianceSum / sz);

                sb.append(String.format(
                    "[%s] Count:%d Mean:%.2f Median:%.2f StdDev:%.2f Min:%.2f Max:%.2f || ",
                    colName, sz, mean, median, stdDev, min, max));
            }

            if (!anyNumeric) {
                // No numbers found — report text column info instead (single line)
                StringBuilder fallback = new StringBuilder(
                    "No numeric columns found. Rows: " + (rows.length - 1) + " Columns: " + numCols + " || ");
                for (int c = 0; c < numCols; c++) {
                    Set<String> unique = new HashSet<>();
                    for (int r = 1; r < rows.length; r++) {
                        String row = rows[r].trim();
                        if (row.isEmpty()) continue;
                        String[] cells = row.split(",");
                        if (c < cells.length) unique.add(cells[c].trim());
                    }
                    fallback.append(String.format("[%s] %d unique || ", headers[c].trim(), unique.size()));
                }
                return fallback.toString().trim();
            }

            return sb.toString().trim();

        } catch (Exception e) {
            return "Error processing CSV: " + e.getMessage();
        }
    }

    // Main method for manual testing
    public static void main(String[] args) {
        CSVStatsService service = new CSVStatsService();
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CSV Stats Service Test Bench ===");
        System.out.println("Paste CSV data (header row first):");

        if (scanner.hasNextLine()) {
            String input = scanner.nextLine();
            String result = service.processCSV(input);
            System.out.println(result);
        }

        scanner.close();
    }
}
