package io.github.astromg01.optisystemff.core.model

enum class GameEdition(
    val displayName: String,
    val packageName: String,
    val shortLabel: String,
) {
    FREE_FIRE(
        displayName = "Free Fire",
        packageName = "com.dts.freefireth",
        shortLabel = "FF",
    ),
    FREE_FIRE_MAX(
        displayName = "Free Fire MAX",
        packageName = "com.dts.freefiremax",
        shortLabel = "FF MAX",
    ),
}
