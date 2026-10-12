package com.auralis.player

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

/**
 * Verifica en un dispositivo real que la app arranca (grafo Hilt + tema) y que
 * el drawer muestra la version actual.
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchSmokeTest {

    private val notificationPermissionRule: TestRule =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Evita que el dialogo del sistema tape la UI en Android 13+.
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            TestRule { base, _ -> base }
        }

    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val rules: TestRule = RuleChain
        .outerRule(notificationPermissionRule)
        .around(composeRule)

    @Test
    fun arranca_y_elDrawer_muestraLaVersionDeLaApp() {
        composeRule.onNodeWithContentDescription("Logo Auralis").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Logo Auralis").performClick()

        composeRule.onNodeWithText("v${BuildConfig.VERSION_NAME}").assertIsDisplayed()
    }
}
