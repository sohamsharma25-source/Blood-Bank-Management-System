package bloodbank.thread;

import bloodbank.service.BloodBankSystem;

/**
 * Background worker thread that periodically sweeps inventory for expired blood units.
 * Demonstrates:
 * - Multithreading via Runnable interface
 * - Daemon thread lifecycle
 * - Volatile flag for safe cancellation across threads
 * - InterruptedException handling and Thread.currentThread().interrupt()
 * - Concurrency control with synchronized BloodBankSystem methods
 */
public class ExpirySweeper implements Runnable {
    private final BloodBankSystem system;
    private final long intervalMillis;
    private volatile boolean running = true;

    public ExpirySweeper(BloodBankSystem system) {
        this(system, 30000); // Sweep every 30 seconds by default
    }

    public ExpirySweeper(BloodBankSystem system, long intervalMillis) {
        this.system = system;
        this.intervalMillis = intervalMillis;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Thread-safe call to scan and expire units in the background
                system.sweepExpiredUnits();

                // Sleep for the configured interval
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                // Restore interrupted status and gracefully terminate thread
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Silent fail-safe to avoid killing background daemon
            }
        }
    }
}
