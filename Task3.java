//FIX:

public class BankStatementBatchProcessor {

    // FIX: processedCount++ is not thread-safe because increment is not atomic.
    // Multiple threads update the same variable concurrently, causing lost updates.
    // AtomicInteger ensures thread-safe atomic increment operations.
    private AtomicInteger processedCount = new AtomicInteger(0);

    public void process(List<StatementRecord> records) {
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (StatementRecord record : records) {
            executor.submit(() -> {
                processRecord(record);
                // FIX: incrementAndGet() is a single atomic CAS operation — thread-safe.
                processedCount.incrementAndGet();
            });
        }
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);
    }

    public int getProcessedCount() {
        return processedCount.get(); // FIX: .get() reflects latest value across all threads.
    }
}