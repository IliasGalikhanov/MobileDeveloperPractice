package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.UiState

class MainViewModel : ViewModel() {
    private val _state = MutableLiveData<UiState>()
    val state: LiveData<UiState> = _state

    fun calculate(amountStr: String, discountStr: String) {
        val amount = amountStr.toDoubleOrNull()
        val discount = discountStr.toDoubleOrNull()

        when {
            amount == null || discount == null ->
                _state.value = UiState(errorText = "Заполните все поля")
            amount < 0 || discount < 0 ->
                _state.value = UiState(errorText = "Числа не могут быть отрицательными")
            discount > 90 ->
                _state.value = UiState(errorText = "Скидка не может быть > 90%")
            else -> {
                val result = amount * (1 - discount / 100)
                _state.value = UiState(resultText = "Итого: $result тенге", isValid = true)
            }
        }
    }
}