package com.example.seniorservicesapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seniorservicesapp.AuthService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(navController: NavController) {
    var userRequests by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Fetch Requests
    LaunchedEffect(Unit) {
        AuthService.getUserRequests { requests ->
            userRequests = requests
        }
    }

    var selectedFilter by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = "Back")
        }

        // Dropdown Filter
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedFilter,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter Requests") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .clickable { expanded = true }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                listOf("All", "Pending", "Approved", "Rejected").forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            selectedFilter = status
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Requests
        val filteredRequests = userRequests.filter {
            selectedFilter == "All" || it["status"].toString().equals(selectedFilter, ignoreCase = true)
        }

        // Display Requests
        LazyColumn {
            items(filteredRequests) { request ->
                RequestItem(request)
            }
        }
    }
}
