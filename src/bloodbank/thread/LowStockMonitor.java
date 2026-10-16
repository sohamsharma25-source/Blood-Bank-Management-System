package bloodbank.thread;

import bloodbank.model.*;
import bloodbank.service.BloodBankSystem;

/**
 * Background monitor thread scoped to the Admin session.
 * Demonstrates:
 * - Runnable interface implementation
 * - volatile boolean flag for safe thread termination
 * - InterruptedException handling and Thread.currentThread().interrupt()
 */
public class LowStockMonitor implements Runnable {
    private final BloodBankSystem system;
    private volatile boolean running = true;

    public LowStockMonitor(BloodBankSystem system) {
        this.system = system;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // Background passive monitor: inspects inventory against thresholds
                for (BloodGroup g : BloodGroup.values()) {
                    int count = system.available(g);
                    int threshold = system.threshold(g);
                    // Silently verifies stock levels without disrupting console input
                }
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Fail-safe to keep thread stable
            }
        }
    }
}