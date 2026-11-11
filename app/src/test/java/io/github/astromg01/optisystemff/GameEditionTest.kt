package io.github.astromg01.optisystemff

import io.github.astromg01.optisystemff.core.model.GameEdition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GameEditionTest {
    @Test
    fun supportedGamesUseSeparatePackageIdentifiers() {
        assertEquals("com.dts.freefireth", GameEdition.FREE_FIRE.packageName)
        assertEquals("com.dts.freefiremax", GameEdition.FREE_FIRE_MAX.packageName)
        assertNotEquals(GameEdition.FREE_FIRE.packageName, GameEdition.FREE_FIRE_MAX.packageName)
    }
}
