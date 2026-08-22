package com.commanderanalyst

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.commanderanalyst.ui.CommanderAnalystApp
import com.commanderanalyst.ui.theme.CommanderAnalystTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CommanderAnalystTheme {
                CommanderAnalystApp()
            }
        }
    }
}
