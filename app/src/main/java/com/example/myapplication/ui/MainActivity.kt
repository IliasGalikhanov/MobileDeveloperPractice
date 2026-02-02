package com.example.myapplication.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.viewmodel.DiceViewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: DiceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rollButton: Button = findViewById(R.id.rollButton)
        val diceImage: ImageView = findViewById(R.id.diceImage)

        viewModel.currentRoll.observe(this) { number ->
            val drawableResource = when (number) {
                1 -> R.drawable.dice_1
                2 -> R.drawable.dice_2
                3 -> R.drawable.dice_3
                4 -> R.drawable.dice_4
                5 -> R.drawable.dice_5
                else -> R.drawable.dice_6
            }
            diceImage.setImageResource(drawableResource)
        }

        rollButton.setOnClickListener { viewModel.rollDice() }
    }
}