package org.game24.marketsync.game.antibrightness;

import org.game24.marketsync.config.AntiBrightnessConfig;
import org.game24.marketsync.game.antibrightness.AntiBrightnessTracker.SuspicionResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntiBrightnessTrackerTest {

    private static final AntiBrightnessConfig CONFIG = new AntiBrightnessConfig(
            true,
            1,
            2000,
            3,
            10000,
            4,
            15000,
            10,
            10,
            "marketsync.antibrightness.bypass",
            "warning"
    );

    @Test
    void record_withMinInterval_ignoresTooFrequentWarnings() {
        AntiBrightnessTracker tracker = new AntiBrightnessTracker(CONFIG);
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.ofEpochMilli(10000);

        assertTrue(tracker.record(uuid, now).recorded());
        assertFalse(tracker.record(uuid, now.plusMillis(1000)).recorded());
        assertTrue(tracker.record(uuid, now.plusMillis(2000)).recorded());
    }

    @Test
    void record_afterWarningThreshold_sendsWarningOnce() {
        AntiBrightnessTracker tracker = new AntiBrightnessTracker(CONFIG);
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.ofEpochMilli(10000);

        assertFalse(tracker.record(uuid, now).warningRequired());
        assertFalse(tracker.record(uuid, now.plusMillis(2000)).warningRequired());

        SuspicionResult third = tracker.record(uuid, now.plusMillis(4000));
        assertTrue(third.warningRequired());
        assertFalse(third.punishmentRequired());

        SuspicionResult fourth = tracker.record(uuid, now.plusMillis(6000));
        assertFalse(fourth.warningRequired());
        assertTrue(fourth.punishmentRequired());
    }

    @Test
    void decay_afterInterval_removesEmptyPlayerState() {
        AntiBrightnessTracker tracker = new AntiBrightnessTracker(CONFIG);
        UUID uuid = UUID.randomUUID();
        Instant now = Instant.ofEpochMilli(10000);

        tracker.record(uuid, now);
        tracker.decay(now.plusSeconds(10));

        assertEquals(0, tracker.trackedPlayers());
    }
}
