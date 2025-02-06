package com.example.seniorservicesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.example.seniorservicesapp.ui.AdminDashboardScreen
import com.example.seniorservicesapp.ui.LoginScreen
import com.example.seniorservicesapp.ui.SignupScreen
import com.example.seniorservicesapp.ui.DashboardScreen
import com.example.seniorservicesapp.ui.RequestsScreen
import com.example.seniorservicesapp.ui.theme.SeniorServicesAppTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeniorServicesAppTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationHost(navController, innerPadding)
                }
            }
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController,
        startDestination = "login",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("login") { LoginScreen(navController) }
        composable("signup") { SignupScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable("admin_dashboard") { AdminDashboardScreen(navController) }
        composable("requests") { RequestsScreen(navController) }
    }
}


