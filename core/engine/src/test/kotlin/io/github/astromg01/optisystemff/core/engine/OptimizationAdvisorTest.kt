package io.github.astromg01.optisystemff.core.engine

import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.ThermalState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OptimizationAdvisorTest {
    private val advisor = OptimizationAdvisor()

    @Test
    fun healthySnapshotDoesNotCreateWarnings() {
        val findings = advisor.evaluate(snapshot())
        assertTrue(findings.isEmpty())
    }

    @Test
    fun lowAvailableSystemMemoryCreatesWarning() {
        val findings = advisor.evaluate(snapshot(availableRamBytes = 500L * MB))
        assertEquals("DIAG-RAM-LOW", findings.single().id)
        assertEquals(FindingSeverity.WARNING, findings.single().severity)
    }

    @Test
    fun platformLowMemorySignalTakesPriorityOverRatio() {
        val findings = advisor.evaluate(
            snapshot(
                availableRamBytes = 4L * GB,
                isSystemLowMemory = true,
            ),
        )

        assertEquals("DIAG-RAM-LOW", findings.single().id)
        assertEquals(FindingSeverity.WARNING, findings.single().severity)
    }

    @Test
    fun powerSaverModeCreatesAnEvidenceBasedAttentionFinding() {
        val findings = advisor.evaluate(snapshot(isPowerSaveMode = true))
        assertEquals("DIAG-POWER-SAVER", findings.single().id)
        assertEquals(FindingSeverity.ATTENTION, findings.single().severity)
    }

    @Test
    fun moderateThermalStateCreatesAttentionFinding() {
        val findings = advisor.evaluate(snapshot(thermalState = ThermalState.MODERATE))
        assertEquals("DIAG-THERMAL", findings.single().id)
        assertEquals(FindingSeverity.ATTENTION, findings.single().severity)
    }

    @Test
    fun criticalThermalStateCreatesWarning() {
        val findings = advisor.evaluate(snapshot(thermalState = ThermalState.CRITICAL))
        assertEquals(FindingSeverity.WARNING, findings.single().severity)
    }

    @Test
    fun lowStorageCreatesAttentionFinding() {
        val findings = advisor.evaluate(snapshot(availableStorageBytes = 5L * GB))
        assertEquals("DIAG-STORAGE-LOW", findings.single().id)
        assertEquals(FindingSeverity.ATTENTION, findings.single().severity)
    }

    private fun snapshot(
        availableRamBytes: Long = 4L * GB,
        availableStorageBytes: Long = 100L * GB,
        thermalState: ThermalState = ThermalState.NONE,
        isSystemLowMemory: Boolean = false,
        isPowerSaveMode: Boolean = false,
    ) = DeviceSnapshot(
        manufacturer = "Samsung",
        model = "SM-A065M",
        androidRelease = "16",
        apiLevel = 36,
        totalRamBytes = 6L * GB,
        availableRamBytes = availableRamBytes,
        totalStorageBytes = 128L * GB,
        availableStorageBytes = availableStorageBytes,
        thermalState = thermalState,
        capturedAtEpochMillis = 1L,
        isSystemLowMemory = isSystemLowMemory,
        isPowerSaveMode = isPowerSaveMode,
    )

    private companion object {
        const val MB = 1024L * 1024L
        const val GB = 1024L * MB
    }
}
