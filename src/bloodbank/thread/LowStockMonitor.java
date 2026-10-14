package bloodbank.thread;

import bloodbank.model.*;
import bloodbank.service.BloodBankSystem;

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
                for (BloodGroup g : BloodGroup.values()) {
                    if (system.available(g) < system.threshold(g)) {
                        System.out.println("[LOW-STOCK MONITOR] " + g.label() + " has " + system.available(g) + " available unit(s).");
                    }
                }
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Monitor error: " + e.getMessage());
            }
        }
    }
}