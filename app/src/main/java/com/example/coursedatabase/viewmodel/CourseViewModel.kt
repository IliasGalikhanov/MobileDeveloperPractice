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

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: CourseRepository
    val allCourses: LiveData<List<Course>>
    
    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> = _selectedCourse
    
    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        val database = AppDatabase.getDatabase(application)
        val courseDao = database.courseDao()
        repository = CourseRepository(courseDao)
        allCourses = repository.allCourses
        loadStatistics()
    }
    
    fun insert(course: Course) {
        viewModelScope.launch {
            try {
                repository.insert(course)
                loadStatistics()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при добавлении курса: ${e.message}"
            }
        }
    }
    
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
    
    fun searchCourses(query: String): LiveData<List<Course>> {
        return repository.searchCourses(query)
    }
    
    fun filterByRating(minRating: Float): LiveData<List<Course>> {
        return repository.getCoursesByRating(minRating)
    }
    
    fun filterByPrice(minPrice: Double, maxPrice: Double): LiveData<List<Course>> {
        return repository.getCoursesByPriceRange(minPrice, maxPrice)
    }
    
    fun getTopCourses(limit: Int = 5): LiveData<List<Course>> {
        return repository.getTopCourses(limit)
    }
    
    fun update(course: Course) {
        viewModelScope.launch {
            try {
                val updated = repository.update(course)
                if (updated > 0) {
                    loadStatistics()
                } else {
                    _errorMessage.value = "Курс не найден"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении: ${e.message}"
            }
        }
    }
    
    fun updatePrice(courseId: Long, newPrice: Double) {
        viewModelScope.launch {
            try {
                repository.updatePrice(courseId, newPrice)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении цены: ${e.message}"
            }
        }
    }
    
    fun enrollToCourse(courseId: Long, currentStudentsCount: Int, currentRating: Float) {
        viewModelScope.launch {
            try {
                val newStudentsCount = currentStudentsCount + 1
                val newRating = (currentRating + 0.01f).coerceAtMost(5.0f)
                repository.updateRatingAndStudents(courseId, newRating, newStudentsCount)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при записи: ${e.message}"
            }
        }
    }
    
    fun delete(course: Course) {
        viewModelScope.launch {
            try {
                val deleted = repository.delete(course)
                if (deleted > 0) {
                    loadStatistics()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении: ${e.message}"
            }
        }
    }
    
    fun deleteAll() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
                loadStatistics()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении всех курсов: ${e.message}"
            }
        }
    }
    
    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }
    
    fun clearSelection() {
        _selectedCourse.value = null
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
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
                // ignore
            }
        }
    }
    
    data class Statistics(
        val coursesCount: Int = 0,
        val averagePrice: Double = 0.0,
        val totalStudents: Int = 0
    )
}
