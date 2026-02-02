package com.example.myapplication.ui

import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val amountInput: EditText = findViewById(R.id.amountInput)
        val discountInput: EditText = findViewById(R.id.discountInput)
        val calcButton: Button = findViewById(R.id.calcButton)
        val resultText: TextView = findViewById(R.id.resultText)
        val errorText: TextView = findViewById(R.id.errorText)

        // Наблюдаем за состоянием [cite: 53]
        viewModel.state.observe(this) { state ->
            resultText.text = state.resultText
            errorText.text = state.errorText
        }

        calcButton.setOnClickListener {
            viewModel.calculate(
                amountInput.text.toString(),
                discountInput.text.toString()
            )
        }
    }
}