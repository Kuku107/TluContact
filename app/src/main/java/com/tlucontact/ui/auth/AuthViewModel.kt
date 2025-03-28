package com.tlucontact.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun register(email: String, password: String, displayName: String) {
        // Validate email domain
        if (!isValidUniversityEmail(email)) {
            _authState.value = AuthState.Error("Email phải thuộc tên miền @tlu.edu.vn hoặc @e.tlu.edu.vn")
            return
        }

        // Register with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // Determine role based on email domain
                    val role = if (email.endsWith("@e.tlu.edu.vn")) "SV" else "CBGV"

                    // Create user document in Firestore
                    db.collection("users").document(user.uid)
                        .set(mapOf(
                            "uid" to user.uid,
                            "email" to email,
                            "displayName" to displayName,
                            "role" to role
                        ))
                        .addOnSuccessListener {
                            // Send email verification
                            user.sendEmailVerification()
                            _authState.value = AuthState.Registered
                        }
                        .addOnFailureListener { e ->
                            _authState.value = AuthState.Error("Lỗi tạo hồ sơ: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Đăng ký thất bại: ${e.message}")
            }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authState.value = AuthState.LoggedIn
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Đăng nhập thất bại: ${e.message}")
            }
    }

    fun resetPassword(email: String) {
        if (!isValidUniversityEmail(email)) {
            _authState.value = AuthState.Error("Email phải thuộc tên miền @tlu.edu.vn hoặc @e.tlu.edu.vn")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _authState.value = AuthState.ResetEmailSent
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error("Không thể gửi email đặt lại: ${e.message}")
            }
    }

    private fun isValidUniversityEmail(email: String): Boolean {
        return email.endsWith("@tlu.edu.vn") || email.endsWith("@e.tlu.edu.vn")
    }

    sealed class AuthState {
        object LoggedIn : AuthState()
        object Registered : AuthState()
        object ResetEmailSent : AuthState()
        data class Error(val message: String) : AuthState()
    }
}