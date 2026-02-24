package com.example.coursedatabase.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "instructor")
    val instructor: String,
    
    @ColumnInfo(name = "duration")
    val duration: String,
    
    @ColumnInfo(name = "price")
    val price: Double,
    
    @ColumnInfo(name = "rating")
    val rating: Float,
    
    @ColumnInfo(name = "students_count")
    val studentsCount: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun getSampleCourses(): List<Course> {
            return listOf(
                Course(
                    title = "Android Development с Kotlin",
                    description = "Полный курс разработки Android приложений с нуля",
                    instructor = "Асхат Аманжолов",
                    duration = "40 часов",
                    price = 15990.0,
                    rating = 4.8f,
                    studentsCount = 2543
                ),
                Course(
                    title = "Jetpack Compose",
                    description = "Современный UI toolkit для Android",
                    instructor = "Айгерим Султанова",
                    duration = "30 часов",
                    price = 12990.0,
                    rating = 4.9f,
                    studentsCount = 1876
                ),
                Course(
                    title = "Kotlin Coroutines и Flow",
                    description = "Асинхронное программирование в Kotlin",
                    instructor = "Бахытжан Назарбаев",
                    duration = "25 часов",
                    price = 9990.0,
                    rating = 4.7f,
                    studentsCount = 1234
                ),
                Course(
                    title = "MVVM и Clean Architecture",
                    description = "Архитектура Android приложений",
                    instructor = "Динара Касымова",
                    duration = "35 часов",
                    price = 13990.0,
                    rating = 4.6f,
                    studentsCount = 987
                ),
                Course(
                    title = "Room Database",
                    description = "Работа с локальной базой данных",
                    instructor = "Ербол Жумагулов",
                    duration = "20 часов",
                    price = 7990.0,
                    rating = 4.5f,
                    studentsCount = 1456
                ),
                Course(
                    title = "Retrofit и REST API",
                    description = "Сетевые запросы в Android",
                    instructor = "Самат Искаков",
                    duration = "18 часов",
                    price = 6990.0,
                    rating = 4.8f,
                    studentsCount = 2109
                ),
                Course(
                    title = "Material Design 3",
                    description = "Современный дизайн Android приложений",
                    instructor = "Гульнара Ахметова",
                    duration = "22 часов",
                    price = 8990.0,
                    rating = 4.7f,
                    studentsCount = 1654
                )
            )
        }
    }
}
