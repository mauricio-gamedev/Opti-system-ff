package io.github.astromg01.optisystemff.core.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.ThermalState

class DeviceDiagnostics(context: Context) {
    private val appContext = context.applicationContext

    fun capture(): DeviceSnapshot {
        val activityManager = appContext.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val dataStats = StatFs(Environment.getDataDirectory().absolutePath)
        val totalStorage = dataStats.blockSizeLong * dataStats.blockCountLong
        val availableStorage = dataStats.blockSizeLong * dataStats.availableBlocksLong

        return DeviceSnapshot(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            androidRelease = Build.VERSION.RELEASE.orEmpty(),
            apiLevel = Build.VERSION.SDK_INT,
            totalRamBytes = memoryInfo.totalMem.coerceAtLeast(0L),
            availableRamBytes = memoryInfo.availMem.coerceAtLeast(0L),
            totalStorageBytes = totalStorage.coerceAtLeast(0L),
            availableStorageBytes = availableStorage.coerceAtLeast(0L),
            thermalState = readThermalState(),
            capturedAtEpochMillis = System.currentTimeMillis(),
        )
    }

    private fun readThermalState(): ThermalState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ThermalState.UNAVAILABLE
        }

        val powerManager = appContext.getSystemService(PowerManager::class.java)
            ?: return ThermalState.UNAVAILABLE

        val platformStatus = runCatching { powerManager.currentThermalStatus }
            .getOrNull()
            ?: return ThermalState.UNAVAILABLE

        return when (platformStatus) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalState.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalState.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalState.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalState.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalState.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalState.EMERGENCY
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalState.SHUTDOWN
            else -> ThermalState.UNAVAILABLE
        }
    }
}
