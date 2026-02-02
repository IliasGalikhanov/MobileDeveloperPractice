package com.example.myapplication.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Dice

class DiceViewModel : ViewModel() {
    private val dice = Dice()
    val currentRoll = MutableLiveData<Int>()

    fun rollDice() {
        currentRoll.value = dice.roll()
    }
}