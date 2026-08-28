package com.auralis.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.auralis.player.core.ui.theme.ReproductorTheme
import com.auralis.player.feature.navigation.ReproductorNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReproductorTheme {
                ReproductorNavHost()
            }
        }
    }
}
