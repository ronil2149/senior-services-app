package com.example.seniorservicesapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seniorservicesapp.AuthService
import java.util.*

@Composable
fun AdminDashboardScreen(navController: NavController) {
    LocalContext.current
    var requests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Fetch requests when the screen is opened
    LaunchedEffect(Unit) {
        AuthService.getAllRequests { fetchedRequests ->
            requests = fetchedRequests
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Text(
            text = "Admin Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Requests Section
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            if (requests.isEmpty()) {
                Text(
                    text = "No requests found",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    text = "Service Requests",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(requests) { request ->
                        AdminRequestCard(request)
                    }
                }
            }
        }

        // Logout Button
        OutlinedButton(
            onClick = { showLogoutDialog = true }, // Open logout confirmation dialog
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Logout,
                contentDescription = "Logout",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            AuthService.logout()
                            navController.navigate("login") {
                                popUpTo("admin_dashboard") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@Composable
fun AdminRequestCard(request: Map<String, Any>) {
    val timestamp = request["timestamp"] as? Long ?: 0L
    val formattedTime = formatTimestamp(timestamp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Service: ${request["serviceType"]}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Status: ${request["status"]}",
                style = MaterialTheme.typography.bodyMedium,
                color = getStatusColor(request["status"] as? String ?: "pending")
            )
            Text(
                text = "Requester: ${request["userFullName"]}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Phone: ${request["userPhone"]}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Requested: $formattedTime",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun getStatusColor(status: String): androidx.compose.ui.graphics.Color {
    return when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.primary
        "approved" -> MaterialTheme.colorScheme.tertiary
        "completed" -> MaterialTheme.colorScheme.secondary
        "rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
}

