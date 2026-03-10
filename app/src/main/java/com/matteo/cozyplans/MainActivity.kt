package com.matteo.cozyplans

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.matteo.cozyplans.ui.CozyPlansApp
import com.matteo.cozyplans.ui.theme.CozyPlansTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CozyPlansTheme {
                CozyPlansApp()
            }
        }
    }
}
