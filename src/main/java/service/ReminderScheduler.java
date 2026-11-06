package service;

import config.DatabaseConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background scheduler for automatic service reminder evaluations.
 * Employs ScheduledExecutorService with daemon threads to prevent dangling JVM processes.
 */
public class ReminderScheduler {
    private static ReminderScheduler instance;
    private ScheduledExecutorService scheduler;
    private boolean running = false;

    private ReminderScheduler() {}

    public static synchronized ReminderScheduler getInstance() {
        if (instance == null) {
            instance = new ReminderScheduler();
        }
        return instance;
    }

    /**
     * Starts the periodic background check.
     */
    public synchronized void start() {
        if (running) return;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ReminderScheduler-Thread");
            t.setDaemon(true);
            return t;
        });

        int interval = DatabaseConfig.getReminderCheckIntervalMinutes();

        // Run initial check after 3 seconds, then repeat every configured interval
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                int generated = ReminderService.getInstance().checkAllVehicles();
                if (generated > 0) {
                    System.out.println("[ReminderEngine] Automatic check generated " + generated + " new service reminders.");
                }
            } catch (Exception e) {
                System.err.println("[ReminderEngine] Background error: " + e.getMessage());
            }
        }, 3, interval * 60L, TimeUnit.SECONDS);

        running = true;
        System.out.println("[ReminderEngine] Background service reminder scheduler started (interval: " + interval + "m).");
    }

    /**
     * Immediately triggers an out-of-band evaluation.
     */
    public void triggerImmediateCheck() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.execute(() -> ReminderService.getInstance().checkAllVehicles());
        } else {
            ReminderService.getInstance().checkAllVehicles();
        }
    }

    /**
     * Gracefully stops the background scheduler.
     */
    public synchronized void stop() {
        if (!running) return;
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
        running = false;
        System.out.println("[ReminderEngine] Background scheduler stopped.");
    }
}
