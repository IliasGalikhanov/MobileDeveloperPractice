package com.example.coursemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursemanager.model.Course

class CourseViewModel : ViewModel() {

    private val _courses = MutableLiveData<List<Course>>()
    
    val courses: LiveData<List<Course>> = _courses

    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> = _selectedCourse

    private var operationCount = 0

    init {
        loadCourses()
    }

    fun loadCourses() {
        _courses.value = Course.getSampleCourses()
        logOperation("Загружено ${_courses.value?.size} курсов")
    }

    fun addCourse(course: Course) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList + course
        _courses.value = newList
        logOperation("Добавлен курс: ${course.title}")
    }

    fun deleteCourse(courseId: String) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.filter { it.id != courseId }
        _courses.value = newList
        logOperation("Удален курс с ID: $courseId")
    }

    fun updateCourse(updatedCourse: Course) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.map { course ->
            if (course.id == updatedCourse.id) {
                updatedCourse
            } else {
                course
            }
        }
        _courses.value = newList
        logOperation("Обновлен курс: ${updatedCourse.title}")
    }

    fun increaseCoursePrice(courseId: String, percentIncrease: Double) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.map { course ->
            if (course.id == courseId) {
                course.copy(price = course.price * (1 + percentIncrease / 100))
            } else {
                course
            }
        }
        _courses.value = newList
        logOperation("Цена курса $courseId увеличена на $percentIncrease%")
    }

    fun updateCourseRating(courseId: String, newRating: Float) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.map { course ->
            if (course.id == courseId) {
                course.copy(rating = newRating)
            } else {
                course
            }
        }
        _courses.value = newList
        logOperation("Рейтинг курса $courseId обновлен до $newRating")
    }

    fun selectCourse(course: Course) {
        _selectedCourse.value = course
    }

    fun clearSelection() {
        _selectedCourse.value = null
    }

    fun filterByRating(minRating: Float) {
        val allCourses = Course.getSampleCourses()
        val filtered = allCourses.filter { it.rating >= minRating }
        _courses.value = filtered
        logOperation("Применен фильтр: рейтинг >= $minRating")
    }

    fun sortByPrice(ascending: Boolean = true) {
        val currentList = _courses.value.orEmpty()
        val sorted = if (ascending) {
            currentList.sortedBy { it.price }
        } else {
            currentList.sortedByDescending { it.price }
        }
        _courses.value = sorted
        logOperation("Сортировка по цене: ${if (ascending) "возрастание" else "убывание"}")
    }

    fun resetFilters() {
        loadCourses()
        logOperation("Фильтры сброшены")
    }

    fun getCourseById(courseId: String): Course? {
        return _courses.value?.find { it.id == courseId }
    }

    private fun logOperation(message: String) {
        operationCount++
        println("[$operationCount] CourseViewModel: $message")
    }
}
