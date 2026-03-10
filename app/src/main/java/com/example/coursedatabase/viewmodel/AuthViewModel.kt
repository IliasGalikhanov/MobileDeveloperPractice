package com.example.coursedatabase.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    
    private val _authState = MutableLiveData<AuthUiState>()
    val authState: LiveData<AuthUiState> = _authState

    private val _currentUser = MutableLiveData(auth.currentUser)
    val currentUser: LiveData<com.google.firebase.auth.FirebaseUser?> = _currentUser

    fun login(email: String, pass: String, context: Context) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthUiState.Error("Заполните все поля")
            return
        }

        _authState.value = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _currentUser.value = auth.currentUser
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error(task.exception?.message ?: "Ошибка входа")
                }
            }
    }

    fun register(email: String, pass: String, role: String, context: Context) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthUiState.Error("Заполните все поля")
            return
        }

        _authState.value = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveRole(context, task.result?.user?.uid ?: "", role)
                    _currentUser.value = auth.currentUser
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error(task.exception?.message ?: "Ошибка регистрации")
                }
            }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }

    private fun saveRole(context: Context, uid: String, role: String) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("role_$uid", role).apply()
    }

    fun getUserRole(context: Context): String {
        val uid = auth.currentUser?.uid ?: return "student"
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getString("role_$uid", "student") ?: "student"
    }

    sealed class AuthUiState {
        object Loading : AuthUiState()
        object Success : AuthUiState()
        data class Error(val message: String) : AuthUiState()
    }
}
