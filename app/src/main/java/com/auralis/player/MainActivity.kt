package com.auralis.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auralis.player.core.ui.theme.ReproductorTheme
import com.auralis.player.data.repository.ThemeController
import com.auralis.player.feature.navigation.ReproductorNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeController: ThemeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeController.themeMode.collectAsStateWithLifecycle()
            ReproductorTheme(themeMode = themeMode) {
                ReproductorNavHost()
            }
        }
    }
}
