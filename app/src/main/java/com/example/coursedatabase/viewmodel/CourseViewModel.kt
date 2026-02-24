package com.example.coursedatabase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.coursedatabase.data.database.AppDatabase
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.data.repository.CourseRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для управления данными курсов
 * 
 * AndroidViewModel - расширение ViewModel с доступом к Application context
 * Нужен для создания Database instance
 * 
 * ViewModel живет дольше Activity/Fragment и переживает пересоздание экрана
 * (поворот экрана, изменение конфигурации)
 */
class CourseViewModel(application: Application) : AndroidViewModel(application) {
    
    /**
     * Repository для работы с данными
     * 
     * Создается один раз при создании ViewModel
     */
    private val repository: CourseRepository
    
    /**
     * LiveData со всеми курсами из БД
     * 
     * Автоматически обновляется при изменениях в БД
     * Room + LiveData работают реактивно!
     */
    val allCourses: LiveData<List<Course>>
    
    /**
     * LiveData для выбранного курса (для деталей)
     */
    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> = _selectedCourse
    
    /**
     * LiveData для статистики
     */
    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics
    
    /**
     * LiveData для сообщений об ошибках
     */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        // Получаем БД и создаем Repository
        val database = AppDatabase.getDatabase(application)
        val courseDao = database.courseDao()
        repository = CourseRepository(courseDao)
        
        // Получаем LiveData со всеми курсами
        allCourses = repository.allCourses
        
        // Загружаем статистику
        loadStatistics()
    }
    
    /**
     * CREATE - Добавление курса
     * 
     * viewModelScope - корутина, привязанная к жизненному циклу ViewModel
     * Автоматически отменяется при уничтожении ViewModel
     * 
     * launch - запускает корутину в фоновом потоке
     */
    fun insert(course: Course) {
        viewModelScope.launch {
            try {
                val id = repository.insert(course)
                println("CourseViewModel: Курс добавлен с ID: $id")
                loadStatistics()  // Обновляем статистику
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при добавлении курса: ${e.message}"
            }
        }
    }
    
    /**
     * CREATE - Добавление с валидацией
     */
    fun insertWithValidation(course: Course, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val result = repository.addCourseWithValidation(course)
            result.onSuccess { id ->
                onSuccess(id)
                loadStatistics()
            }.onFailure { error ->
                onError(error.message ?: "Неизвестная ошибка")
            }
        }
    }
    
    /**
     * READ - Поиск курсов
     */
    fun searchCourses(query: String): LiveData<List<Course>> {
        return repository.searchCourses(query)
    }
    
    /**
     * READ - Фильтрация по рейтингу
     */
    fun filterByRating(minRating: Float): LiveData<List<Course>> {
        return repository.getCoursesByRating(minRating)
    }
    
    /**
     * READ - Фильтрация по цене
     */
    fun filterByPrice(minPrice: Double, maxPrice: Double): LiveData<List<Course>> {
        return repository.getCoursesByPriceRange(minPrice, maxPrice)
    }
    
    /**
     * READ - Топ курсов
     */
    fun getTopCourses(limit: Int = 5): LiveData<List<Course>> {
        return repository.getTopCourses(limit)
    }
    
    /**
     * UPDATE - Обновление курса
     */
    fun update(course: Course) {
        viewModelScope.launch {
            try {
                val updated = repository.update(course)
                if (updated > 0) {
                    println("CourseViewModel: Курс обновлен")
                    loadStatistics()
                } else {
                    _errorMessage.value = "Курс не найден"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении: ${e.message}"
            }
        }
    }
    
    /**
     * UPDATE - Обновление цены
     */
    fun updatePrice(courseId: Long, newPrice: Double) {
        viewModelScope.launch {
            try {
                repository.updatePrice(courseId, newPrice)
                println("CourseViewModel: Цена обновлена")
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении цены: ${e.message}"
            }
        }
    }
    
    /**
     * UPDATE - "Записаться на курс" - увеличивает количество студентов
     */
    fun enrollToCourse(courseId: Long, currentStudentsCount: Int, currentRating: Float) {
        viewModelScope.launch {
            try {
                // Увеличиваем количество студентов
                val newStudentsCount = currentStudentsCount + 1
                
                // Немного увеличиваем рейтинг (имитация положительного отзыва)
                val newRating = (currentRating + 0.01f).coerceAtMost(5.0f)
                
                repository.updateRatingAndStudents(courseId, newRating, newStudentsCount)
                println("CourseViewModel: Записались на курс. Студентов: $newStudentsCount")
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при записи: ${e.message}"
            }
        }
    }
    
    /**
     * DELETE - Удаление курса
     */
    fun delete(course: Course) {
        viewModelScope.launch {
            try {
                val deleted = repository.delete(course)
                if (deleted > 0) {
                    println("CourseViewModel: Курс удален")
                    loadStatistics()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении: ${e.message}"
            }
        }
    }
    
    /**
     * DELETE - Удаление всех курсов
     */
    fun deleteAll() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
                println("CourseViewModel: Все курсы удалены")
                loadStatistics()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении всех курсов: ${e.message}"
            }
        }
    }
    
    /**
     * Выбор курса для отображения деталей
     */
    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }
    
    /**
     * Очистка выбранного курса
     */
    fun clearSelection() {
        _selectedCourse.value = null
    }
    
    /**
     * Очистка сообщения об ошибке
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Загрузка статистики
     */
    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                val count = repository.getCoursesCount()
                val averagePrice = repository.getAveragePrice()
                val totalStudents = repository.getTotalStudents()
                
                _statistics.value = Statistics(
                    coursesCount = count,
                    averagePrice = averagePrice,
                    totalStudents = totalStudents
                )
            } catch (e: Exception) {
                println("CourseViewModel: Ошибка загрузки статистики: ${e.message}")
            }
        }
    }
    
    /**
     * Класс для статистики
     */
    data class Statistics(
        val coursesCount: Int = 0,
        val averagePrice: Double = 0.0,
        val totalStudents: Int = 0
    )
}
