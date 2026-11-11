package io.github.astromg01.optisystemff.core.model

data class DeviceSnapshot(
    val manufacturer: String,
    val model: String,
    val androidRelease: String,
    val apiLevel: Int,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val thermalState: ThermalState,
    val capturedAtEpochMillis: Long,
) {
    val availableRamRatio: Double?
        get() = totalRamBytes.takeIf { it > 0L }?.let {
            (availableRamBytes.toDouble() / it).coerceIn(0.0, 1.0)
        }

    val availableStorageRatio: Double?
        get() = totalStorageBytes.takeIf { it > 0L }?.let {
            (availableStorageBytes.toDouble() / it).coerceIn(0.0, 1.0)
        }
}

enum class ThermalState {
    UNAVAILABLE,
    NONE,
    LIGHT,
    MODERATE,
    SEVERE,
    CRITICAL,
    EMERGENCY,
    SHUTDOWN,
}
