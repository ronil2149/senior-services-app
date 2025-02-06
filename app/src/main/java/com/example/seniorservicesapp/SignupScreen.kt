package com.example.seniorservicesapp.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.seniorservicesapp.AuthService
import com.example.seniorservicesapp.UserRegistrationData


@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current

    // State variables for all form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Signup", style = MaterialTheme.typography.headlineMedium)

        // Full Name input field
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.padding(8.dp)
        )

        // Phone Number input field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            modifier = Modifier.padding(8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next  // This allows users to move to the next field
            )
        )

// Email input field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.padding(8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

// Password input field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(8.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done  // This indicates it's the last input field
            )
        )

        // Signup Button with enhanced validation
        Button(
            onClick = {
                // Validate all required fields
                when {
                    fullName.isBlank() -> {
                        Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    phoneNumber.isBlank() -> {
                        Toast.makeText(context, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    email.isBlank() -> {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    password.isBlank() -> {
                        Toast.makeText(context, "Please enter a password", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                }

                isLoading = true

                // Create registration data object
                val registrationData = UserRegistrationData(
                    email = email,
                    password = password,
                    fullName = fullName,
                    phoneNumber = phoneNumber
                )

                // Call the enhanced signup function
                AuthService.signUpUser(registrationData) { success, error ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                        navController.navigate("login")
                    } else {
                        Toast.makeText(context, error ?: "Signup failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = if (isLoading) "Signing Up..." else "Sign Up")
        }

        TextButton(onClick = { navController.navigate("login") }) {
            Text("Already have an account? Login")
        }
    }
}
