package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.MhRepository
import com.example.ui.MainScreen
import com.example.ui.MhViewModel
import com.example.ui.MhViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Setup offline local database layer
        val database = AppDatabase.getDatabase(this)
        val repository = MhRepository(database)

        // Instantiate state managers
        val factory = MhViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[MhViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
