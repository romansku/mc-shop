package org.game24.marketsync.game.antibrightness;

import org.game24.marketsync.config.AntiBrightnessConfig;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class AntiBrightnessDecayJob implements Runnable {

    private final AntiBrightnessTracker tracker;

    private final AntiBrightnessConfig config;

    private final Logger logger;

    private volatile boolean running = true;

    public AntiBrightnessDecayJob(AntiBrightnessTracker tracker, AntiBrightnessConfig config, Logger logger) {
        this.tracker = tracker;
        this.config = config;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                long waitMillis = config.decayIntervalSeconds() * 1000L;
                TimeUnit.MILLISECONDS.sleep(waitMillis);
                tracker.decay(Instant.now());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.warn("Anti-brightness decay failed", e);
            }
        }
    }

    public void stop() {
        running = false;
    }
}
