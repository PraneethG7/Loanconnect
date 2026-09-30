package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.LoanConnectApp
import com.example.ui.theme.LoanConnectTheme
import com.example.ui.viewmodel.LoanConnectViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LoanConnectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LoanConnectTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LoanConnectApp(viewModel = viewModel)
                }
            }
        }
    }
}
