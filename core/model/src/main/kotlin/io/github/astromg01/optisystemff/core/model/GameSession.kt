package io.github.astromg01.optisystemff.core.model

data class GameSession(
    val id: String,
    val gameEdition: GameEdition,
    val startedAtEpochMillis: Long,
    val startSnapshot: DeviceSnapshot,
    val endedAtEpochMillis: Long? = null,
    val endSnapshot: DeviceSnapshot? = null,
) {
    val isFinished: Boolean
        get() = endedAtEpochMillis != null && endSnapshot != null
}
