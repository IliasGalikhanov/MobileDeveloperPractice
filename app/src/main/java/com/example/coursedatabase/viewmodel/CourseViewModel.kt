package com.example.coursedatabase.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.data.repository.CourseRepository
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {

    private val db = Firebase.firestore
    private val COURSES_COLLECTION = "courses"
    private var coursesListener: ListenerRegistration? = null

    val allCourses: LiveData<List<Course>> = repository.allCourses.asLiveData()

    private val _statistics = MutableLiveData<Statistics>()
    val statistics: LiveData<Statistics> = _statistics

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        startFirestoreListener()
    }

    fun startFirestoreListener() {
        coursesListener?.remove()
        _isLoading.value = true
        
        coursesListener = db.collection(COURSES_COLLECTION)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _errorMessage.value = "Ошибка Firestore: ${e.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val courses = snapshot.documents.mapNotNull { doc ->
                        try {
                            Course(
                                id = 0,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                instructor = doc.getString("instructor") ?: "",
                                duration = doc.getString("duration") ?: "Не указана",
                                price = doc.getDouble("price") ?: 0.0,
                                rating = doc.getDouble("rating")?.toFloat() ?: 0.0f,
                                studentsCount = doc.getLong("studentsCount")?.toInt() ?: 0,
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                firestoreId = doc.id
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    viewModelScope.launch {
                        repository.deleteAll()
                        repository.insertAll(courses)
                        loadStatistics()
                    }
                }
            }
    }

    fun fetchCoursesFromFirestore() {
        startFirestoreListener()
    }

    fun insert(course: Course) {
        viewModelScope.launch {
            try {
                val courseData = createCourseMap(course)
                db.collection(COURSES_COLLECTION).add(courseData).await()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при добавлении: ${e.message}"
            }
        }
    }

    fun update(course: Course) {
        viewModelScope.launch {
            try {
                if (course.firestoreId != null) {
                    val courseData = createCourseMap(course)
                    db.collection(COURSES_COLLECTION).document(course.firestoreId).set(courseData).await()
                } else {
                    // Если ID нет (старый курс), ищем по названию или просто обновляем локально
                    repository.update(course)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при обновлении: ${e.message}"
            }
        }
    }

    fun delete(course: Course) {
        viewModelScope.launch {
            try {
                if (course.firestoreId != null) {
                    db.collection(COURSES_COLLECTION).document(course.firestoreId).delete().await()
                } else {
                    repository.delete(course)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при удалении: ${e.message}"
            }
        }
    }

    private fun createCourseMap(course: Course): Map<String, Any> {
        return hashMapOf(
            "title" to course.title,
            "description" to course.description,
            "instructor" to course.instructor,
            "duration" to course.duration,
            "price" to course.price,
            "rating" to course.rating,
            "studentsCount" to course.studentsCount,
            "createdAt" to course.createdAt
        )
    }

    fun enrollToCourse(courseId: Long, currentStudentsCount: Int, currentRating: Float) {
        viewModelScope.launch {
            try {
                // Находим курс в текущем списке, чтобы получить его firestoreId
                val course = allCourses.value?.find { it.id == courseId }
                if (course?.firestoreId != null) {
                    val newStudentsCount = currentStudentsCount + 1
                    val newRating = (currentRating + 0.01f).coerceAtMost(5.0f)
                    
                    db.collection(COURSES_COLLECTION).document(course.firestoreId).update(
                        "studentsCount", newStudentsCount,
                        "rating", newRating
                    ).await()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка при записи: ${e.message}"
            }
        }
    }

    fun clearError() { _errorMessage.value = null }

    private fun loadStatistics() {
        viewModelScope.launch {
            val count = repository.getCoursesCount()
            val averagePrice = repository.getAveragePrice()
            val totalStudents = repository.getTotalStudents()
            _statistics.value = Statistics(count, averagePrice, totalStudents)
        }
    }

    override fun onCleared() {
        super.onCleared()
        coursesListener?.remove()
    }

    data class Statistics(val coursesCount: Int, val averagePrice: Double, val totalStudents: Int)
}
