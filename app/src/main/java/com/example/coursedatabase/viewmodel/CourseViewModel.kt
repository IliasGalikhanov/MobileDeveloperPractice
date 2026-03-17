package com.example.coursedatabase.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.coursedatabase.data.dto.PostDto
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.data.repository.CourseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {

    private val db = Firebase.firestore
    private val COURSES_COLLECTION = "courses"

    val allCourses: LiveData<List<Course>> = repository.allCourses.asLiveData()

    private val _postsState = MutableStateFlow<UiState<List<PostDto>>>(UiState.Loading)
    val postsState: StateFlow<UiState<List<PostDto>>> = _postsState.asStateFlow()

    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        loadStatistics()
        fetchPosts()
        // При запуске можно попробовать синхронизировать данные с Firestore
        // syncCoursesWithFirestore()
    }

    // --- Firestore Methods (Practice 12) ---

    fun uploadCourseToFirestore(course: Course) {
        viewModelScope.launch {
            try {
                // Преобразуем объект Course в Map для Firestore
                val courseData = hashMapOf(
                    "title" to course.title,
                    "description" to course.description,
                    "instructor" to course.instructor,
                    "price" to course.price,
                    "rating" to course.rating,
                    "studentsCount" to course.studentsCount,
                    "createdAt" to course.createdAt
                )
                
                db.collection(COURSES_COLLECTION)
                    .add(courseData)
                    .await()
                
                Log.d("Firestore", "Course successfully uploaded!")
            } catch (e: Exception) {
                Log.e("Firestore", "Error uploading course", e)
                _errorMessage.value = "Ошибка Firestore: ${e.message}"
            }
        }
    }

    fun syncCoursesFromFirestore() {
        viewModelScope.launch {
            try {
                val result = db.collection(COURSES_COLLECTION).get().await()
                val firestoreCourses = result.documents.mapNotNull { doc ->
                    // Здесь логика конвертации из DocumentSnapshot в локальный объект
                    // Для простоты просто выведем в лог количество
                }
                Log.d("Firestore", "Fetched ${result.size()} courses from cloud")
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки из облака: ${e.message}"
            }
        }
    }

    // --- Practice 13: Debug & Test helper ---
    fun calculateDiscountPrice(price: Double, discountPercent: Int): Double {
        // Нарочно допустим ошибку для Practice 13, если нужно будет её найти через отладчик
        if (discountPercent < 0) return price
        return price * (1 - discountPercent / 100.0)
    }

    // --- Existing Methods ---

    fun fetchPosts() {
        viewModelScope.launch {
            _postsState.value = UiState.Loading
            delay(1000)
            try {
                val posts = repository.fetchPosts()
                _postsState.value = UiState.Success(posts)
            } catch (e: Exception) {
                _postsState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun insert(course: Course) {
        viewModelScope.launch {
            try {
                repository.insert(course)
                loadStatistics()
                // Автоматически дублируем в облако (Practice 12)
                uploadCourseToFirestore(course)
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
                uploadCourseToFirestore(course)
            }.onFailure { error ->
                onError(error.message ?: "Неизвестная ошибка")
            }
        }
    }

    fun searchCourses(query: String): LiveData<List<Course>> {
        return repository.searchCourses(query).asLiveData()
    }

    fun filterByRating(minRating: Float): LiveData<List<Course>> {
        return repository.getCoursesByRating(minRating).asLiveData()
    }

    fun filterByPrice(minPrice: Double, maxPrice: Double): LiveData<List<Course>> {
        return repository.getCoursesByPriceRange(minPrice, maxPrice).asLiveData()
    }

    fun getTopCourses(limit: Int = 5): LiveData<List<Course>> {
        return repository.getTopCourses(limit).asLiveData()
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
