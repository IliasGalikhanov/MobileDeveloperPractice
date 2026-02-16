package com.example.coursemanager.model

import java.util.UUID
data class Course(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val instructor: String,
    val duration: String,
    val price: Double,
    val rating: Float,
    val studentsCount: Int
) {
    companion object {
        fun getSampleCourses(): List<Course> {
            return listOf(
                Course(
                    id = "1",
                    title = "Android Development с Kotlin",
                    description = "Полный курс разработки Android приложений с нуля",
                    instructor = "Арман Ахметов",
                    duration = "40 часов",
                    price = 85000.0,
                    rating = 4.8f,
                    studentsCount = 2543
                ),
                Course(
                    id = "2",
                    title = "Jetpack Compose",
                    description = "Современный UI toolkit для Android",
                    instructor = "Гульнара Касымова",
                    duration = "30 часов",
                    price = 65000.0,
                    rating = 4.9f,
                    studentsCount = 1876
                ),
                Course(
                    id = "3",
                    title = "Kotlin Coroutines и Flow",
                    description = "Асинхронное программирование в Kotlin",
                    instructor = "Даурен Смагулов",
                    duration = "25 часов",
                    price = 45000.0,
                    rating = 4.7f,
                    studentsCount = 1234
                ),
                Course(
                    id = "4",
                    title = "MVVM и Clean Architecture",
                    description = "Архитектура Android приложений",
                    instructor = "Айгерим Оспанова",
                    duration = "35 часов",
                    price = 75000.0,
                    rating = 4.6f,
                    studentsCount = 987
                ),
                Course(
                    id = "5",
                    title = "Room Database",
                    description = "Работа с локальной базой данных",
                    instructor = "Берик Ибраев",
                    duration = "20 часов",
                    price = 35000.0,
                    rating = 4.5f,
                    studentsCount = 1456
                )
            )
        }
    }
}
