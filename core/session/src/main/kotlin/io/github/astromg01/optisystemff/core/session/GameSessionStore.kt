package io.github.astromg01.optisystemff.core.session

import android.content.Context
import android.content.SharedPreferences
import io.github.astromg01.optisystemff.core.model.DeviceSnapshot
import io.github.astromg01.optisystemff.core.model.GameEdition
import io.github.astromg01.optisystemff.core.model.GameSession
import io.github.astromg01.optisystemff.core.model.ThermalState
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Small, bounded, local-only session history.
 *
 * The store persists only diagnostic snapshots and timestamps. It does not collect
 * gameplay content, account data, network traffic or information from the game process.
 */
class GameSessionStore(context: Context) {
    private val preferences: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    @Synchronized
    fun activeSession(): GameSession? {
        val raw = preferences.getString(KEY_ACTIVE_SESSION, null) ?: return null
        return decodeSession(raw) ?: run {
            preferences.edit().remove(KEY_ACTIVE_SESSION).apply()
            null
        }
    }

    @Synchronized
    fun recentSessions(limit: Int = DEFAULT_HISTORY_LIMIT): List<GameSession> {
        if (limit <= 0) return emptyList()
        val raw = preferences.getString(KEY_HISTORY, null) ?: return emptyList()
        val sessions = decodeHistory(raw)
        return sessions
            .sortedByDescending(GameSession::startedAtEpochMillis)
            .take(limit)
    }

    @Synchronized
    fun startSession(
        gameEdition: GameEdition,
        startSnapshot: DeviceSnapshot,
        startedAtEpochMillis: Long = System.currentTimeMillis(),
    ): GameSession? {
        if (activeSession() != null) return null

        val session = GameSession(
            id = UUID.randomUUID().toString(),
            gameEdition = gameEdition,
            startedAtEpochMillis = startedAtEpochMillis,
            startSnapshot = startSnapshot,
        )
        preferences.edit()
            .putString(KEY_ACTIVE_SESSION, encodeSession(session).toString())
            .apply()
        return session
    }

    @Synchronized
    fun finishSession(
        endSnapshot: DeviceSnapshot,
        endedAtEpochMillis: Long = System.currentTimeMillis(),
    ): GameSession? {
        val active = activeSession() ?: return null
        val completed = active.copy(
            endedAtEpochMillis = endedAtEpochMillis.coerceAtLeast(active.startedAtEpochMillis),
            endSnapshot = endSnapshot,
        )
        val history = (listOf(completed) + decodeHistory(
            preferences.getString(KEY_HISTORY, null).orEmpty(),
        ))
            .distinctBy(GameSession::id)
            .sortedByDescending(GameSession::startedAtEpochMillis)
            .take(DEFAULT_HISTORY_LIMIT)

        preferences.edit()
            .putString(KEY_HISTORY, encodeHistory(history).toString())
            .remove(KEY_ACTIVE_SESSION)
            .apply()
        return completed
    }

    @Synchronized
    fun discardActiveSession() {
        preferences.edit().remove(KEY_ACTIVE_SESSION).apply()
    }

    private fun decodeHistory(raw: String): List<GameSession> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                decodeSession(item)?.let(::add)
            }
        }
    }.getOrDefault(emptyList())

    private fun decodeSession(raw: String): GameSession? = runCatching {
        decodeSession(JSONObject(raw))
    }.getOrNull()

    private fun decodeSession(json: JSONObject): GameSession? = runCatching {
        val gameEdition = GameEdition.valueOf(json.getString("gameEdition"))
        val startSnapshot = decodeSnapshot(json.getJSONObject("startSnapshot"))
        val endSnapshot = if (json.isNull("endSnapshot")) {
            null
        } else {
            decodeSnapshot(json.getJSONObject("endSnapshot"))
        }
        val endedAt = if (json.isNull("endedAtEpochMillis")) {
            null
        } else {
            json.getLong("endedAtEpochMillis")
        }

        GameSession(
            id = json.getString("id"),
            gameEdition = gameEdition,
            startedAtEpochMillis = json.getLong("startedAtEpochMillis"),
            startSnapshot = startSnapshot,
            endedAtEpochMillis = endedAt,
            endSnapshot = endSnapshot,
        )
    }.getOrNull()

    private fun encodeHistory(sessions: List<GameSession>): JSONArray =
        JSONArray().apply { sessions.forEach { put(encodeSession(it)) } }

    private fun encodeSession(session: GameSession): JSONObject = JSONObject().apply {
        put("id", session.id)
        put("gameEdition", session.gameEdition.name)
        put("startedAtEpochMillis", session.startedAtEpochMillis)
        put("startSnapshot", encodeSnapshot(session.startSnapshot))
        put("endedAtEpochMillis", session.endedAtEpochMillis ?: JSONObject.NULL)
        put("endSnapshot", session.endSnapshot?.let(::encodeSnapshot) ?: JSONObject.NULL)
    }

    private fun encodeSnapshot(snapshot: DeviceSnapshot): JSONObject = JSONObject().apply {
        put("manufacturer", snapshot.manufacturer)
        put("model", snapshot.model)
        put("androidRelease", snapshot.androidRelease)
        put("apiLevel", snapshot.apiLevel)
        put("totalRamBytes", snapshot.totalRamBytes)
        put("availableRamBytes", snapshot.availableRamBytes)
        put("totalStorageBytes", snapshot.totalStorageBytes)
        put("availableStorageBytes", snapshot.availableStorageBytes)
        put("thermalState", snapshot.thermalState.name)
        put("capturedAtEpochMillis", snapshot.capturedAtEpochMillis)
        put("isSystemLowMemory", snapshot.isSystemLowMemory)
        put("memoryThresholdBytes", snapshot.memoryThresholdBytes)
        put("isLowRamDevice", snapshot.isLowRamDevice)
        put("isPowerSaveMode", snapshot.isPowerSaveMode)
        put("batteryPercent", snapshot.batteryPercent ?: JSONObject.NULL)
        put("currentRefreshRateHz", snapshot.currentRefreshRateHz ?: JSONObject.NULL)
        put("supportedRefreshRatesHz", JSONArray(snapshot.supportedRefreshRatesHz))
        put("socManufacturer", snapshot.socManufacturer ?: JSONObject.NULL)
        put("socModel", snapshot.socModel ?: JSONObject.NULL)
        put("hardware", snapshot.hardware ?: JSONObject.NULL)
        put("supportedAbis", JSONArray(snapshot.supportedAbis))
    }

    private fun decodeSnapshot(json: JSONObject): DeviceSnapshot {
        val thermalState = runCatching {
            ThermalState.valueOf(json.optString("thermalState"))
        }.getOrDefault(ThermalState.UNAVAILABLE)

        return DeviceSnapshot(
            manufacturer = json.optString("manufacturer"),
            model = json.optString("model"),
            androidRelease = json.optString("androidRelease"),
            apiLevel = json.optInt("apiLevel"),
            totalRamBytes = json.optLong("totalRamBytes"),
            availableRamBytes = json.optLong("availableRamBytes"),
            totalStorageBytes = json.optLong("totalStorageBytes"),
            availableStorageBytes = json.optLong("availableStorageBytes"),
            thermalState = thermalState,
            capturedAtEpochMillis = json.optLong("capturedAtEpochMillis"),
            isSystemLowMemory = json.optBoolean("isSystemLowMemory"),
            memoryThresholdBytes = json.optLong("memoryThresholdBytes"),
            isLowRamDevice = json.optBoolean("isLowRamDevice"),
            isPowerSaveMode = json.optBoolean("isPowerSaveMode"),
            batteryPercent = nullableInt(json, "batteryPercent")?.takeIf { it in 0..100 },
            currentRefreshRateHz = nullableFloat(json, "currentRefreshRateHz"),
            supportedRefreshRatesHz = readFloatArray(json.optJSONArray("supportedRefreshRatesHz")),
            socManufacturer = nullableString(json, "socManufacturer"),
            socModel = nullableString(json, "socModel"),
            hardware = nullableString(json, "hardware"),
            supportedAbis = readStringArray(json.optJSONArray("supportedAbis")),
        )
    }

    private fun nullableInt(json: JSONObject, key: String): Int? =
        if (json.isNull(key)) null else json.optInt(key)

    private fun nullableFloat(json: JSONObject, key: String): Float? {
        if (json.isNull(key)) return null
        return json.optDouble(key).toFloat().takeIf(Float::isFinite)
    }

    private fun nullableString(json: JSONObject, key: String): String? =
        if (json.isNull(key)) null else json.optString(key).takeIf(String::isNotBlank)

    private fun readFloatArray(array: JSONArray?): List<Float> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                val value = array.optDouble(index).toFloat()
                if (value.isFinite() && value > 0f) add(value)
            }
        }
    }

    private fun readStringArray(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                array.optString(index).takeIf(String::isNotBlank)?.let(::add)
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "opti_system_ff_sessions"
        const val KEY_ACTIVE_SESSION = "active_session"
        const val KEY_HISTORY = "session_history"
        const val DEFAULT_HISTORY_LIMIT = 20
    }
}
