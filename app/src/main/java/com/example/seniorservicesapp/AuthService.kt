package com.example.seniorservicesapp

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


object AuthService {
    // Firebase instance initialization using lazy loading
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    // Collection names as constants to prevent typos and enable easy updates
    private const val USERS_COLLECTION = "users"
    private const val REQUESTS_COLLECTION = "requests"



    fun signUpUser(
        registrationData: UserRegistrationData,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Input validation for all required fields
        if (registrationData.email.isBlank() || registrationData.password.isBlank() ||
            registrationData.fullName.isBlank() || registrationData.phoneNumber.isBlank()
        ) {
            onResult(false, "Please fill in all required fields!")
            return
        }

        // Create user in Firebase Authentication
        auth.createUserWithEmailAndPassword(registrationData.email, registrationData.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        // Create enhanced user document in Firestore
                        val userData = mapOf(
                            "email" to registrationData.email,
                            "fullName" to registrationData.fullName,
                            "phoneNumber" to registrationData.phoneNumber,
                            "userrole" to "user"
                        )
                        firestore.collection(USERS_COLLECTION).document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, "Signup successful, but failed to save user data: ${e.localizedMessage}")
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
        // Input validation
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter both email and password!", null)
            return
        }

        // Authenticate user
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    if (userId != null) {
                        // Fetch user role from Firestore
                        firestore.collection(USERS_COLLECTION).document(userId)
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
                    println("Firebase Login Error: $errorMessage")
                    onResult(false, errorMessage, null)
                }
            }
    }

    fun logout() {
        auth.signOut()
    }


    fun submitRequest(serviceType: String, onResult: (Boolean, String) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(false, "User not logged in")
            return
        }

        // First fetch user details, then create the request
        firestore.collection(USERS_COLLECTION).document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val request = mapOf(
                    "userId" to userId,
                    "serviceType" to serviceType,
                    "status" to "pending",
                    "timestamp" to System.currentTimeMillis(),
                    "userFullName" to (userDoc.getString("fullName") ?: "Unknown"),
                    "userPhone" to (userDoc.getString("phoneNumber") ?: "Unknown")
                )

                firestore.collection(REQUESTS_COLLECTION).add(request)
                    .addOnSuccessListener {
                        onResult(true, "Request submitted successfully!")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, "Failed to submit request: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, "Failed to fetch user details: ${e.localizedMessage}")
            }
    }

    fun getUserRequests(onResult: (List<Map<String, Any>>) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            onResult(emptyList())
            return
        }

        firestore.collection(REQUESTS_COLLECTION)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val requests = querySnapshot.documents.map { it.data ?: emptyMap() }
                onResult(requests)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getAllRequests(onResult: (List<Map<String, Any>>) -> Unit) {
        firestore.collection(REQUESTS_COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val requests = querySnapshot.documents.mapNotNull { document ->
                    val data = document.data
                    if (data != null) {
                        // Include document ID in the request data for reference
                        data["id"] = document.id
                        data
                    } else null
                }
                onResult(requests)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
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