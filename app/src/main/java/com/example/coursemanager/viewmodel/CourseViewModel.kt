package com.example.coursemanager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.coursemanager.model.Course

/**
 * ViewModel для управления списком курсов
 * 
 * Использует LiveData для реактивного обновления UI
 * Все операции создают новый список для корректной работы DiffUtil
 */
class CourseViewModel : ViewModel() {

    // Приватный изменяемый список
    private val _courses = MutableLiveData<List<Course>>()
    
    // Публичный неизменяемый список для наблюдения
    val courses: LiveData<List<Course>> = _courses

    // Выбранный курс для отображения деталей
    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> = _selectedCourse

    // Счетчик операций для логирования
    private var operationCount = 0

    init {
        loadCourses()
    }

    /**
     * Загрузка начальных данных
     */
    fun loadCourses() {
        _courses.value = Course.getSampleCourses()
        logOperation("Загружено ${_courses.value?.size} курсов")
    }

    /**
     * Добавление нового курса
     * 
     * ВАЖНО: Создается НОВЫЙ список для корректной работы DiffUtil
     * submitList() не сработает, если передать тот же объект списка
     */
    fun addCourse(course: Course) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList + course  // Создаем новый список
        _courses.value = newList
        logOperation("Добавлен курс: ${course.title}")
    }

    /**
     * Удаление курса по ID
     * 
     * ВАЖНО: Используем toMutableList() для создания НОВОГО списка
     */
    fun deleteCourse(courseId: String) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.filter { it.id != courseId }  // Создаем новый список
        _courses.value = newList
        logOperation("Удален курс с ID: $courseId")
    }

    /**
     * Обновление существующего курса
     * 
     * ВАЖНО: Используем map для создания НОВОГО списка
     * Даже если изменяется только один элемент, создаем новый список
     */
    fun updateCourse(updatedCourse: Course) {
        val currentList = _courses.value.orEmpty()
        val newList = currentList.map { course ->
            if (course.id == updatedCourse.id) {
                updatedCourse  // Заменяем на обновленный курс
            } else {
                course  // Оставляем без изменений
            }
        }
        _courses.value = newList
        logOperation("Обновлен курс: ${updatedCourse.title}")
    }

    /**
     * Увеличение цены курса на заданный процент
     * Демонстрирует работу DiffUtil при частичном изменении
     */
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

    /**
     * Обновление рейтинга курса
     */
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

    /**
     * Выбор курса для просмотра деталей
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
     * Фильтрация курсов по минимальному рейтингу
     */
    fun filterByRating(minRating: Float) {
        val allCourses = Course.getSampleCourses()
        val filtered = allCourses.filter { it.rating >= minRating }
        _courses.value = filtered
        logOperation("Применен фильтр: рейтинг >= $minRating")
    }

    /**
     * Сортировка по цене
     */
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

    /**
     * Сброс всех фильтров
     */
    fun resetFilters() {
        loadCourses()
        logOperation("Фильтры сброшены")
    }

    /**
     * Получение курса по ID
     */
    fun getCourseById(courseId: String): Course? {
        return _courses.value?.find { it.id == courseId }
    }

    /**
     * Логирование операций (для отладки)
     */
    private fun logOperation(message: String) {
        operationCount++
        println("[$operationCount] CourseViewModel: $message")
    }
}
