package com.example.coursedatabase.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity класс для таблицы courses в Room Database
 * 
 * @Entity - аннотация Room, указывающая что это таблица БД
 * tableName = "courses" - имя таблицы в SQLite
 * 
 * Каждое поле класса становится колонкой в таблице
 */
@Entity(tableName = "courses")
data class Course(
    /**
     * @PrimaryKey - первичный ключ таблицы
     * autoGenerate = true - автоматическая генерация ID при вставке
     * 
     * Каждая запись получает уникальный ID автоматически
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    /**
     * @ColumnInfo - настройка колонки
     * name = "..." - имя колонки в БД (можно отличаться от имени поля)
     */
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
    
    /**
     * Timestamp создания записи
     * По умолчанию - текущее время
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Генерирует демонстрационные курсы для первого запуска
         */
        fun getSampleCourses(): List<Course> {
            return listOf(
                Course(
                    title = "Android Development с Kotlin",
                    description = "Полный курс разработки Android приложений с нуля",
                    instructor = "Иван Петров",
                    duration = "40 часов",
                    price = 15990.0,
                    rating = 4.8f,
                    studentsCount = 2543
                ),
                Course(
                    title = "Jetpack Compose",
                    description = "Современный UI toolkit для Android",
                    instructor = "Мария Сидорова",
                    duration = "30 часов",
                    price = 12990.0,
                    rating = 4.9f,
                    studentsCount = 1876
                ),
                Course(
                    title = "Kotlin Coroutines и Flow",
                    description = "Асинхронное программирование в Kotlin",
                    instructor = "Алексей Смирнов",
                    duration = "25 часов",
                    price = 9990.0,
                    rating = 4.7f,
                    studentsCount = 1234
                ),
                Course(
                    title = "MVVM и Clean Architecture",
                    description = "Архитектура Android приложений",
                    instructor = "Елена Козлова",
                    duration = "35 часов",
                    price = 13990.0,
                    rating = 4.6f,
                    studentsCount = 987
                ),
                Course(
                    title = "Room Database",
                    description = "Работа с локальной базой данных",
                    instructor = "Дмитрий Волков",
                    duration = "20 часов",
                    price = 7990.0,
                    rating = 4.5f,
                    studentsCount = 1456
                ),
                Course(
                    title = "Retrofit и REST API",
                    description = "Сетевые запросы в Android",
                    instructor = "Сергей Новиков",
                    duration = "18 часов",
                    price = 6990.0,
                    rating = 4.8f,
                    studentsCount = 2109
                ),
                Course(
                    title = "Material Design 3",
                    description = "Современный дизайн Android приложений",
                    instructor = "Ольга Морозова",
                    duration = "22 часов",
                    price = 8990.0,
                    rating = 4.7f,
                    studentsCount = 1654
                )
            )
        }
    }
}
