package io.github.astromg01.optisystemff.core.engine

import io.github.astromg01.optisystemff.core.model.GameSession
import io.github.astromg01.optisystemff.core.model.ThermalState

data class SessionComparison(
    val durationMillis: Long,
    val availableRamDeltaBytes: Long,
    val thermalSeverityDelta: Int?,
    val systemLowMemoryBecameActive: Boolean,
    val powerSaveModeChanged: Boolean,
)

/**
 * Compares two system snapshots surrounding a user-recorded game session.
 *
 * The result describes system-wide changes only. It cannot attribute those changes
 * exclusively to the game and it does not measure in-game FPS or frame time.
 */
class SessionComparisonEngine {
    fun compare(session: GameSession): SessionComparison? {
        val endedAt = session.endedAtEpochMillis ?: return null
        val endSnapshot = session.endSnapshot ?: return null
        val startThermalSeverity = thermalSeverity(session.startSnapshot.thermalState)
        val endThermalSeverity = thermalSeverity(endSnapshot.thermalState)

        return SessionComparison(
            durationMillis = (endedAt - session.startedAtEpochMillis).coerceAtLeast(0L),
            availableRamDeltaBytes =
                endSnapshot.availableRamBytes - session.startSnapshot.availableRamBytes,
            thermalSeverityDelta = if (
                startThermalSeverity != null && endThermalSeverity != null
            ) {
                endThermalSeverity - startThermalSeverity
            } else {
                null
            },
            systemLowMemoryBecameActive =
                !session.startSnapshot.isSystemLowMemory && endSnapshot.isSystemLowMemory,
            powerSaveModeChanged =
                session.startSnapshot.isPowerSaveMode != endSnapshot.isPowerSaveMode,
        )
    }

    private fun thermalSeverity(state: ThermalState): Int? = when (state) {
        ThermalState.UNAVAILABLE -> null
        ThermalState.NONE -> 0
        ThermalState.LIGHT -> 1
        ThermalState.MODERATE -> 2
        ThermalState.SEVERE -> 3
        ThermalState.CRITICAL -> 4
        ThermalState.EMERGENCY -> 5
        ThermalState.SHUTDOWN -> 6
    }
}
