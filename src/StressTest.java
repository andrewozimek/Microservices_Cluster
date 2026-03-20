import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import config.ConfigLoader;

public class StressTest {

    static AtomicInteger success = new AtomicInteger(0);
    static AtomicInteger failed  = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        int numClients = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        String host    = ConfigLoader.getServerHost();
        int port       = ConfigLoader.getServerTcpPort();

        System.out.println("Stress testing with " + numClients + " concurrent clients → " + host + ":" + port);

        CountDownLatch ready  = new CountDownLatch(numClients); // all threads wait here
        CountDownLatch start  = new CountDownLatch(1);          // fire them all at once
        CountDownLatch done   = new CountDownLatch(numClients);

        for (int i = 0; i < numClients; i++) {
            final int id = i;
            new Thread(() -> {
                ready.countDown();
                try {
                    start.await(); // wait for the gun to fire
                    runClient(id, host, port);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            }).start();
        }

        ready.await();           // wait until all threads are ready
        long startTime = System.currentTimeMillis();
        start.countDown();       // fire!

        done.await();            // wait for all to finish
        long elapsed = System.currentTimeMillis() - startTime;

        System.out.println("\n--- Results ---");
        System.out.println("Success : " + success.get());
        System.out.println("Failed  : " + failed.get());
        System.out.println("Time    : " + elapsed + "ms");
        System.out.println("Avg/req : " + (elapsed / numClients) + "ms");
    }

    private static void runClient(int id, String host, int port) {
        try (
            Socket socket = new Socket(host, port);
            BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter    out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Read available services
            String services = in.readLine();
            if (services == null || services.startsWith("ERROR")) {
                System.out.println("[" + id + "] ERROR - no services: " + services);
                failed.incrementAndGet();
                return;
            }

            // Send a Base64 encode request
            out.println("Base64EncodeDecode|ENCODE|StressTest" + id);

            String response = in.readLine();
            if (response != null && response.startsWith("OK")) {
                System.out.println("[" + id + "] OK → " + response);
                success.incrementAndGet();
            } else {
                System.out.println("[" + id + "] FAILED → " + response);
                failed.incrementAndGet();
            }

        } catch (Exception e) {
            System.out.println("[" + id + "] EXCEPTION → " + e.getMessage());
            failed.incrementAndGet();
        }
    }
}
