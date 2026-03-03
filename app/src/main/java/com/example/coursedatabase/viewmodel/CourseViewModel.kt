package com.example.coursedatabase.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursedatabase.data.dto.PostDto
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.data.repository.CourseRepository
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {

    val allCourses: LiveData<List<Course>> = repository.allCourses

    private val _postsState = MutableLiveData<UiState<List<PostDto>>>()
    val postsState: LiveData<UiState<List<PostDto>>> = _postsState

    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadStatistics()
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _postsState.value = UiState.Loading
            val result = repository.fetchPosts()
            result.onSuccess { posts ->
                _postsState.value = UiState.Success(posts)
            }.onFailure { error ->
                _postsState.value = UiState.Error(error.message ?: "Неизвестная ошибка")
            }
        }
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
