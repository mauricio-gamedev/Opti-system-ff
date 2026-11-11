package io.github.astromg01.optisystemff.core.engine

import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.GameEdition
import io.github.astromg01.optisystemff.core.model.GameSession
import io.github.astromg01.optisystemff.core.model.ThermalState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionComparisonEngineTest {
    private val engine = SessionComparisonEngine()

    @Test
    fun unfinishedSessionHasNoComparison() {
        assertNull(engine.compare(session(endedAt = null, endSnapshot = null)))
    }

    @Test
    fun comparisonReportsSystemSnapshotDeltas() {
        val result = engine.compare(
            session(
                startedAt = 1_000L,
                endedAt = 61_000L,
                startSnapshot = snapshot(
                    availableRamBytes = 2_000L,
                    thermalState = ThermalState.LIGHT,
                ),
                endSnapshot = snapshot(
                    availableRamBytes = 1_500L,
                    thermalState = ThermalState.SEVERE,
                    isSystemLowMemory = true,
                ),
            ),
        )

        requireNotNull(result)
        assertEquals(60_000L, result.durationMillis)
        assertEquals(-500L, result.availableRamDeltaBytes)
        assertEquals(2, result.thermalSeverityDelta)
        assertTrue(result.systemLowMemoryBecameActive)
    }

    @Test
    fun unavailableThermalReadingProducesUnknownDelta() {
        val result = engine.compare(
            session(
                startSnapshot = snapshot(thermalState = ThermalState.UNAVAILABLE),
                endSnapshot = snapshot(thermalState = ThermalState.NONE),
            ),
        )

        requireNotNull(result)
        assertNull(result.thermalSeverityDelta)
    }

    @Test
    fun unchangedPowerModeIsNotReportedAsChanged() {
        val result = engine.compare(
            session(
                startSnapshot = snapshot(isPowerSaveMode = true),
                endSnapshot = snapshot(isPowerSaveMode = true),
            ),
        )

        requireNotNull(result)
        assertFalse(result.powerSaveModeChanged)
    }

    private fun session(
        startedAt: Long = 1L,
        endedAt: Long? = 2L,
        startSnapshot: DeviceSnapshot = snapshot(),
        endSnapshot: DeviceSnapshot? = snapshot(),
    ) = GameSession(
        id = "test-session",
        gameEdition = GameEdition.FREE_FIRE,
        startedAtEpochMillis = startedAt,
        startSnapshot = startSnapshot,
        endedAtEpochMillis = endedAt,
        endSnapshot = endSnapshot,
    )

    private fun snapshot(
        availableRamBytes: Long = 4_000L,
        thermalState: ThermalState = ThermalState.NONE,
        isSystemLowMemory: Boolean = false,
        isPowerSaveMode: Boolean = false,
    ) = DeviceSnapshot(
        manufacturer = "Samsung",
        model = "SM-A065M",
        androidRelease = "16",
        apiLevel = 36,
        totalRamBytes = 6_000L,
        availableRamBytes = availableRamBytes,
        totalStorageBytes = 100_000L,
        availableStorageBytes = 70_000L,
        thermalState = thermalState,
        capturedAtEpochMillis = 1L,
        isSystemLowMemory = isSystemLowMemory,
        isPowerSaveMode = isPowerSaveMode,
    )
}
