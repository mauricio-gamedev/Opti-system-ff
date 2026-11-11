package io.github.astromg01.optisystemff.core.games

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import io.github.astromg01.optisystemff.core.model.GameEdition

class GameCatalog(context: Context) {
    private val packageManager = context.applicationContext.packageManager

    fun installedGames(): Set<GameEdition> = GameEdition.entries
        .filterTo(linkedSetOf()) { isInstalled(it) }

    fun isInstalled(game: GameEdition): Boolean = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                game.packageName,
                PackageManager.ApplicationInfoFlags.of(0L),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getApplicationInfo(game.packageName, 0)
        }
    }.isSuccess

    fun launchIntent(game: GameEdition): Intent? {
        if (!isInstalled(game)) return null
        return packageManager.getLaunchIntentForPackage(game.packageName)
    }
}
