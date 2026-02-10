package com.example.coursecatalog.model

data class Course(
    val id: Int,
    val title: String,
    val description: String,
    val instructor: String,
    val duration: String,
    val price: Double,
    val rating: Float,
    val studentsCount: Int
)
