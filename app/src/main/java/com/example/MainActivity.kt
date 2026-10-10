package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.BzOneApp
import com.example.ui.theme.BzOneTheme
import com.example.viewmodel.BzOneViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: BzOneViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BzOneTheme {
        BzOneApp(viewModel = viewModel)
      }
    }
  }
}
