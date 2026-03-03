package com.control_delivery.finanzas_delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.control_delivery.finanzas_delivery.ui.components.botton_nav_bar.AppBottomBar
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Finanzas_deliveryTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = { AppBottomBar(navController) }
                ) {
                    innerPadding ->
                    AppNavigation(
                        navController,
                        modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}