package com.example.coursecatalog.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursecatalog.model.Course

class CourseViewModel : ViewModel() {

    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> = _selectedCourse

    init {
        loadCourses()
    }

    private fun loadCourses() {
        val courseList = listOf(
            Course(
                id = 1,
                title = "Android Development с Kotlin",
                description = "Полный курс разработки мобильных приложений под Android с использованием Kotlin",
                instructor = "Иван Петров",
                duration = "12 недель",
                price = 159900.0,
                rating = 4.8f,
                studentsCount = 2543
            ),
            Course(
                id = 2,
                title = "Jetpack Compose для начинающих",
                description = "Современный подход к созданию UI в Android с помощью Jetpack Compose",
                instructor = "Мария Сидорова",
                duration = "8 недель",
                price = 129900.0,
                rating = 4.9f,
                studentsCount = 1876
            ),
            Course(
                id = 3,
                title = "Kotlin Coroutines и Flow",
                description = "Асинхронное программирование в Kotlin: от основ до продвинутых техник",
                instructor = "Алексей Иванов",
                duration = "6 недель",
                price = 99900.0,
                rating = 4.7f,
                studentsCount = 1234
            ),
            Course(
                id = 4,
                title = "MVVM и Clean Architecture",
                description = "Архитектурные паттерны для построения масштабируемых Android-приложений",
                instructor = "Дмитрий Козлов",
                duration = "10 недель",
                price = 139900.0,
                rating = 4.6f,
                studentsCount = 987
            ),
            Course(
                id = 5,
                title = "Room Database и SQLite",
                description = "Работа с локальной базой данных в Android приложениях",
                instructor = "Ольга Новикова",
                duration = "5 недель",
                price = 79900.0,
                rating = 4.5f,
                studentsCount = 1456
            ),
            Course(
                id = 6,
                title = "Retrofit и работа с API",
                description = "Работа с сетевыми запросами и REST API в Android",
                instructor = "Сергей Морозов",
                duration = "4 недели",
                price = 69900.0,
                rating = 4.8f,
                studentsCount = 2109
            ),
            Course(
                id = 7,
                title = "Material Design 3 на практике",
                description = "Создание красивых и современных интерфейсов с Material Design 3",
                instructor = "Анна Волкова",
                duration = "6 недель",
                price = 89900.0,
                rating = 4.7f,
                studentsCount = 1654
            ),
            Course(
                id = 8,
                title = "Firebase для Android",
                description = "Backend as a Service: аутентификация, база данных, хранилище и аналитика",
                instructor = "Павел Соколов",
                duration = "7 недель",
                price = 109900.0,
                rating = 4.6f,
                studentsCount = 1321
            ),
            Course(
                id = 9,
                title = "Testing в Android",
                description = "Unit тесты, UI тесты и инструментальное тестирование приложений",
                instructor = "Елена Смирнова",
                duration = "5 недель",
                price = 84900.0,
                rating = 4.5f,
                studentsCount = 876
            ),
            Course(
                id = 10,
                title = "Публикация в Google Play",
                description = "Подготовка и публикация Android приложения в Google Play Store",
                instructor = "Михаил Лебедев",
                duration = "3 недели",
                price = 59900.0,
                rating = 4.9f,
                studentsCount = 2876
            ),
            Course(
                id = 11,
                title = "Dagger и Hilt для DI",
                description = "Dependency Injection в Android с использованием Dagger 2 и Hilt",
                instructor = "Артём Кузнецов",
                duration = "6 недель",
                price = 94900.0,
                rating = 4.4f,
                studentsCount = 743
            ),
            Course(
                id = 12,
                title = "Многопоточность в Android",
                description = "Threads, Handlers, AsyncTask и современные подходы к многопоточности",
                instructor = "Виктор Попов",
                duration = "5 недель",
                price = 74900.0,
                rating = 4.6f,
                studentsCount = 1098
            )
        )
        
        _courses.value = courseList
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun clearSelection() {
        _selectedCourse.value = null
    }

    fun filterByRating(minRating: Float) {
        val allCourses = _courses.value ?: return
        _courses.value = allCourses.filter { it.rating >= minRating }
    }

    fun sortByPrice(ascending: Boolean = true) {
        val sortedCourses = if (ascending) {
            _courses.value?.sortedBy { it.price }
        } else {
            _courses.value?.sortedByDescending { it.price }
        }
        _courses.value = sortedCourses
    }

    fun resetFilters() {
        loadCourses()
    }
}
