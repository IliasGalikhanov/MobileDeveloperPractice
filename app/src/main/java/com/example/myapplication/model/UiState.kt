package com.example.myapplication.model

data class UiState(
    val resultText: String = "",
    val errorText: String = "",
    val isValid: Boolean = false
)