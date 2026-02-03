package com.example.userprofile.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.userprofile.model.UserProfile

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        _userProfile.value = UserProfile(
            name = "Илияс Галиханов",
            email = "iliasgalikhanov@example.com",
            bio = "Android разработчик с 5+ летним опытом. Увлекаюсь созданием красивых и функциональных приложений.",
            phone = "+7 (777) 123-45-67",
            location = "Астана, Казахстан",
            memberSince = "Январь 2026"
        )
    }

    fun updateProfile(
        name: String,
        email: String,
        bio: String,
        phone: String,
        location: String,
        avatarUri: Uri? = null
    ) {
        val currentProfile = _userProfile.value ?: return
        
        _userProfile.value = currentProfile.copy(
            name = name,
            email = email,
            bio = bio,
            phone = phone,
            location = location,
            avatarUri = avatarUri ?: currentProfile.avatarUri
        )
    }

    fun updateAvatar(uri: Uri) {
        val currentProfile = _userProfile.value ?: return
        _userProfile.value = currentProfile.copy(avatarUri = uri)
    }
}
