package com.example.userprofile.model

import android.net.Uri

data class UserProfile(
    val name: String,
    val email: String,
    val bio: String,
    val phone: String,
    val location: String,
    val memberSince: String,
    val avatarUri: Uri? = null
)
