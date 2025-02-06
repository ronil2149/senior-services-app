package com.example.seniorservicesapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seniorservicesapp.AuthService
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    // State management for our screen
    val context = LocalContext.current
    var serviceType by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var userRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var showServiceDropdown by remember { mutableStateOf(false) }  // For dropdown control

    // Fetch user's requests when screen opens
    LaunchedEffect(Unit) {
        AuthService.getUserRequests { requests ->
            userRequests = requests
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Welcome Header Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "What service do you need today?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Service Request Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Request a Service",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Dropdown menu for service selection
                ExposedDropdownMenuBox(
                    expanded = showServiceDropdown,
                    onExpandedChange = { showServiceDropdown = it }
                ) {
                    OutlinedTextField(
                        value = serviceType,
                        onValueChange = {},  // ReadOnly field
                        readOnly = true,
                        placeholder = { Text("Select a service") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showServiceDropdown) },
                        modifier = Modifier
                            .menuAnchor()  // This is important for proper dropdown positioning
                            .fillMaxWidth()
                    )

                    // The actual dropdown menu
                    ExposedDropdownMenu(
                        expanded = showServiceDropdown,
                        onDismissRequest = { showServiceDropdown = false }
                    ) {
                        // Snow Removal Option
                        DropdownMenuItem(
                            text = { Text("Snow Removal") },
                            onClick = {
                                serviceType = "Snow Removal"
                                showServiceDropdown = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.AcUnit,
                                    contentDescription = "Snow Removal Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )

                        // Garbage Removal Option
                        DropdownMenuItem(
                            text = { Text("Garbage Removal") },
                            onClick = {
                                serviceType = "Garbage Removal"
                                showServiceDropdown = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Garbage Removal Icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (serviceType.isBlank()) {
                            Toast.makeText(context, "Please select a service", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        AuthService.submitRequest(serviceType) { success, message ->
                            isLoading = false
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            if (success) {
                                serviceType = ""
                                AuthService.getUserRequests { requests ->
                                    userRequests = requests
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Submit Request")
                    }
                }
            }
        }

        // Recent Requests Section
        Text(
            text = "Your Recent Requests",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = { navController.navigate("requests") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Submitted Requests")
        }


        // Logout Button
        OutlinedButton(
            onClick = {
                AuthService.logout()
                navController.navigate("login") {
                    popUpTo("dashboard") { inclusive = true }
                }
            },
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
    }
}

@Composable
fun RequestItem(request: Map<String, Any>) {
    val timestamp = request["timestamp"] as? Long ?: 0L
    val formattedTime = formatTimestamp(timestamp)
    val serviceType = request["serviceType"] as? String ?: ""
    val status = request["status"] as? String ?: "pending"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = serviceType,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = status.capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = getStatusColor(status)
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = when(serviceType) {
                    "Snow Removal" -> Icons.Outlined.AcUnit
                    "Garbage Removal" -> Icons.Outlined.Delete
                    else -> Icons.Outlined.Handyman
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.primary
        "approved" -> MaterialTheme.colorScheme.tertiary
        "completed" -> MaterialTheme.colorScheme.secondary
        "rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
}

fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Unknown time"
    }
}