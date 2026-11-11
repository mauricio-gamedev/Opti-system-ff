package io.github.astromg01.optisystemff.core.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.view.Display
import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.ThermalState
import kotlin.math.roundToInt

/**
 * Read-only device diagnostics.
 *
 * Every capture is on demand. This class does not create a polling loop or a background
 * service, keeping sampling overhead bounded and visible to the user.
 */
class DeviceDiagnostics(context: Context) {
    private val appContext = context.applicationContext

    fun capture(): DeviceSnapshot {
        val activityManager = appContext.getSystemService(ActivityManager::class.java)
        val memoryInfo = ActivityManager.MemoryInfo().also { info ->
            activityManager?.getMemoryInfo(info)
        }

        val dataStats = StatFs(Environment.getDataDirectory().absolutePath)
        val totalStorage = dataStats.blockSizeLong * dataStats.blockCountLong
        val availableStorage = dataStats.blockSizeLong * dataStats.availableBlocksLong

        val powerManager = appContext.getSystemService(PowerManager::class.java)
        val displayInfo = readDisplayInfo()
        val batteryPercent = readBatteryPercent()

        return DeviceSnapshot(
            manufacturer = Build.MANUFACTURER.orEmpty(),
            model = Build.MODEL.orEmpty(),
            androidRelease = Build.VERSION.RELEASE.orEmpty(),
            apiLevel = Build.VERSION.SDK_INT,
            totalRamBytes = memoryInfo.totalMem.coerceAtLeast(0L),
            availableRamBytes = memoryInfo.availMem.coerceAtLeast(0L),
            totalStorageBytes = totalStorage.coerceAtLeast(0L),
            availableStorageBytes = availableStorage.coerceAtLeast(0L),
            thermalState = readThermalState(powerManager),
            capturedAtEpochMillis = System.currentTimeMillis(),
            isSystemLowMemory = memoryInfo.lowMemory,
            memoryThresholdBytes = memoryInfo.threshold.coerceAtLeast(0L),
            isLowRamDevice = activityManager?.isLowRamDevice == true,
            isPowerSaveMode = runCatching { powerManager?.isPowerSaveMode == true }
                .getOrDefault(false),
            batteryPercent = batteryPercent,
            currentRefreshRateHz = displayInfo.first,
            supportedRefreshRatesHz = displayInfo.second,
            socManufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MANUFACTURER.takeIf(String::isNotBlank)
            } else {
                null
            },
            socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Build.SOC_MODEL.takeIf(String::isNotBlank)
            } else {
                null
            },
            hardware = Build.HARDWARE.takeIf(String::isNotBlank),
            supportedAbis = Build.SUPPORTED_ABIS.toList(),
        )
    }

    private fun readBatteryPercent(): Int? {
        val batteryManager = appContext.getSystemService(BatteryManager::class.java)
            ?: return null
        val value = runCatching {
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        }.getOrNull() ?: return null

        return value.takeIf { it in 0..100 }
    }

    private fun readDisplayInfo(): Pair<Float?, List<Float>> {
        val displayManager = appContext.getSystemService(DisplayManager::class.java)
            ?: return null to emptyList()
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            ?: return null to emptyList()

        val currentRate = display.refreshRate.takeIf { it.isFinite() && it > 0f }
        val supportedRates = display.supportedModes
            .asSequence()
            .map { it.refreshRate }
            .filter { it.isFinite() && it > 0f }
            .distinctBy { (it * 10f).roundToInt() }
            .sorted()
            .toList()

        return currentRate to supportedRates
    }

    private fun readThermalState(powerManager: PowerManager?): ThermalState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ThermalState.UNAVAILABLE
        }

        powerManager ?: return ThermalState.UNAVAILABLE
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
