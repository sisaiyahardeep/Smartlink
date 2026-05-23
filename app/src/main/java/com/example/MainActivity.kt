package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.MusicRepository
import com.example.data.SmartLink
import com.example.ui.MusicViewModel
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.PublicSmartLinkPreview
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Core Data Initializations
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MusicRepository(database.musicDao())
        
        // 2. ViewModel construction
        val viewModel = ViewModelProvider(
            this,
            MusicViewModel.Factory(repository)
        )[MusicViewModel::class.java]

        setContent {
            MyApplicationTheme {
                // Main reactive states
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                var activePreviewLink by remember { mutableStateOf<SmartLink?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ) { innerPadding ->
                    // Wrapping everything inside our stunning neon ambient Glassmorphism background!
                    GlassBackground(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Core Route Controller
                        when {
                            activePreviewLink != null -> {
                                PublicSmartLinkPreview(
                                    smartLink = activePreviewLink!!,
                                    viewModel = viewModel,
                                    onBack = { activePreviewLink = null }
                                )
                            }
                            currentUser == null -> {
                                AuthScreen(viewModel = viewModel)
                            }
                            else -> {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onPreviewLink = { activePreviewLink = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
