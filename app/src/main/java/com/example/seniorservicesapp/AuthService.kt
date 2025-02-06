package com.example.seniorservicesapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore

object AuthService {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun signUpUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        val userData = mapOf(
                            "email" to email,
                            "userrole" to "user"
                        )
                        firestore.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Signup successful, but failed to save user role: ${e.localizedMessage}")
                            }
                    } else {
                        onResult(false, "Signup failed: User ID is null")
                    }
                } else {
                    val errorMessage = getAuthErrorMessage(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        firestore.collection("users").document(userId)
                            .get()
                            .addOnSuccessListener { document ->
                                val userRole = document.getString("userrole") ?: "user"
                                onResult(true, null, userRole)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Login successful, but failed to fetch user role: ${e.localizedMessage}", null)
                            }
                    } else {
                        onResult(false, "Login failed: User ID is null", null)
                    }
                } else {
                    val errorMessage = getAuthErrorMessage(task.exception)
                    onResult(false, errorMessage, null)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }

    private fun getAuthErrorMessage(exception: Exception?): String {
        return when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email format. Please enter a valid email."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email. Sign up first."
            "ERROR_USER_DISABLED" -> "This account has been disabled. Contact support."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered. Try logging in."
            "ERROR_WEAK_PASSWORD" -> "Weak password! Use at least 6 characters."
            else -> "Authentication failed. Please check credentials."
        }
    }
}
