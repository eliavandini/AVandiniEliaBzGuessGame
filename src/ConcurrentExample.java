import java.util.concurrent.*;

public class ConcurrentExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Callable<String> inputTask = () -> {
            System.out.println("Enter something: ");
            return new java.util.Scanner(System.in).nextLine();
        };

        Future<String> future = executor.submit(inputTask);

        try {
            String result = future.get(5, TimeUnit.SECONDS); // Wait for 5 seconds
            System.out.println("You entered: " + result);
        } catch (TimeoutException e) {
            System.out.println("Timeout occurred! No input received.");
            future.cancel(true); // Cancel the task
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }
}