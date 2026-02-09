package com.example.coursecatalog.model

/**
 * Модель данных для курса обучения
 * 
 * @param id уникальный идентификатор курса
 * @param title название курса
 * @param description краткое описание курса
 * @param instructor имя преподавателя
 * @param duration продолжительность курса
 * @param price цена курса
 * @param rating рейтинг курса (от 0.0 до 5.0)
 * @param studentsCount количество студентов
 */
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
