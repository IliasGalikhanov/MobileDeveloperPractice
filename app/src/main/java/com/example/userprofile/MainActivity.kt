package com.example.userprofile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.userprofile.databinding.ActivityMainBinding
import com.example.userprofile.databinding.DialogEditProfileBinding
import com.example.userprofile.viewmodel.ProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProfileViewModel by viewModels()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            viewModel.updateAvatar(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Профиль"
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                binding.apply {
                    tvUserName.text = it.name
                    tvUserEmail.text = it.email
                    tvBio.text = it.bio
                    tvPhone.text = it.phone
                    tvLocation.text = it.location
                    tvMemberSince.text = "Участник с ${it.memberSince}"
                    
                    it.avatarUri?.let { uri ->
                        ivAvatar.setImageURI(uri)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnEdit.setOnClickListener {
                showEditDialog()
            }

            cardAvatar.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            btnSettings.setOnClickListener {
                Toast.makeText(this@MainActivity, "Настройки", Toast.LENGTH_SHORT).show()
            }

            btnLogout.setOnClickListener {
                finish()
            }
        }
    }

    private fun showEditDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(this))
        val currentProfile = viewModel.userProfile.value

        currentProfile?.let {
            dialogBinding.etName.setText(it.name)
            dialogBinding.etEmail.setText(it.email)
            dialogBinding.etBio.setText(it.bio)
            dialogBinding.etPhone.setText(it.phone)
            dialogBinding.etLocation.setText(it.location)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Редактировать профиль")
            .setView(dialogBinding.root)
            .setPositiveButton("Сохранить") { _, _ ->
                val name = dialogBinding.etName.text.toString()
                val email = dialogBinding.etEmail.text.toString()
                val bio = dialogBinding.etBio.text.toString()
                val phone = dialogBinding.etPhone.text.toString()
                val location = dialogBinding.etLocation.text.toString()

                if (name.isNotBlank() && email.isNotBlank()) {
                    viewModel.updateProfile(name, email, bio, phone, location)
                    Toast.makeText(this, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Имя и Email обязательны", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
