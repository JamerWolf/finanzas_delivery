package com.control_delivery.finanzas_delivery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.control_delivery.finanzas_delivery.ui.home.HomeScreen
import com.control_delivery.finanzas_delivery.ui.theme.Finanzas_deliveryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Finanzas_deliveryTheme {
                HomeScreen()
            }
        }
    }
}
