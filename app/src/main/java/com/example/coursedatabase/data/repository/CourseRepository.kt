package com.example.coursedatabase.data.repository

import androidx.lifecycle.LiveData
import com.example.coursedatabase.data.dao.CourseDao
import com.example.coursedatabase.data.entity.Course

/**
 * Repository класс для работы с данными курсов
 * 
 * Repository Pattern - архитектурный паттерн, который:
 * 1. Абстрагирует источник данных от ViewModel
 * 2. Объединяет несколько источников данных (БД, API, кэш)
 * 3. Управляет логикой получения данных
 * 
 * ЗАЧЕМ НУЖЕН REPOSITORY?
 * 
 * ❌ БЕЗ Repository:
 * ViewModel → DAO (прямая зависимость от Room)
 * Проблемы:
 * - ViewModel знает о Room
 * - Сложно добавить API или кэш
 * - Сложно тестировать
 * 
 * ✅ С Repository:
 * ViewModel → Repository → DAO
 * Преимущества:
 * - ViewModel не знает об источнике данных
 * - Легко добавить API: Repository → (DAO + API)
 * - Легко тестировать (можно подменить Repository)
 * - Единое место для логики данных
 */
class CourseRepository(private val courseDao: CourseDao) {
    
    /**
     * Получение всех курсов
     * 
     * LiveData автоматически обновляется при изменении БД
     * Room выполняет запрос в фоновом потоке
     */
    val allCourses: LiveData<List<Course>> = courseDao.getAllCourses()
    
    /**
     * CREATE - Добавление курса
     * 
     * suspend - функция должна вызываться из корутины
     * Room автоматически выполняет операцию в фоновом потоке
     * 
     * @return Long - ID добавленного курса
     */
    suspend fun insert(course: Course): Long {
        return courseDao.insert(course)
    }
    
    /**
     * CREATE - Добавление списка курсов
     */
    suspend fun insertAll(courses: List<Course>) {
        courseDao.insertAll(courses)
    }
    
    /**
     * READ - Получение курса по ID
     */
    suspend fun getCourseById(id: Long): Course? {
        return courseDao.getCourseById(id)
    }
    
    /**
     * READ - Поиск курсов
     */
    fun searchCourses(query: String): LiveData<List<Course>> {
        return courseDao.searchCourses(query)
    }
    
    /**
     * READ - Фильтрация по рейтингу
     */
    fun getCoursesByRating(minRating: Float): LiveData<List<Course>> {
        return courseDao.getCoursesByRating(minRating)
    }
    
    /**
     * READ - Фильтрация по цене
     */
    fun getCoursesByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Course>> {
        return courseDao.getCoursesByPriceRange(minPrice, maxPrice)
    }
    
    /**
     * READ - Получение количества курсов
     */
    suspend fun getCoursesCount(): Int {
        return courseDao.getCoursesCount()
    }
    
    /**
     * UPDATE - Обновление курса
     * 
     * @return Int - количество обновленных записей
     */
    suspend fun update(course: Course): Int {
        return courseDao.update(course)
    }
    
    /**
     * UPDATE - Обновление цены
     */
    suspend fun updatePrice(courseId: Long, newPrice: Double) {
        courseDao.updatePrice(courseId, newPrice)
    }
    
    /**
     * UPDATE - Обновление рейтинга и студентов
     */
    suspend fun updateRatingAndStudents(courseId: Long, rating: Float, studentsCount: Int) {
        courseDao.updateRatingAndStudents(courseId, rating, studentsCount)
    }
    
    /**
     * DELETE - Удаление курса
     * 
     * @return Int - количество удаленных записей
     */
    suspend fun delete(course: Course): Int {
        return courseDao.delete(course)
    }
    
    /**
     * DELETE - Удаление по ID
     */
    suspend fun deleteById(courseId: Long): Int {
        return courseDao.deleteById(courseId)
    }
    
    /**
     * DELETE - Удаление всех курсов
     */
    suspend fun deleteAll() {
        courseDao.deleteAll()
    }
    
    /**
     * CUSTOM - Топ курсов по популярности
     */
    fun getTopCourses(limit: Int = 5): LiveData<List<Course>> {
        return courseDao.getTopCourses(limit)
    }
    
    /**
     * CUSTOM - Средняя цена
     */
    suspend fun getAveragePrice(): Double {
        return courseDao.getAveragePrice() ?: 0.0
    }
    
    /**
     * CUSTOM - Общее количество студентов
     */
    suspend fun getTotalStudents(): Int {
        return courseDao.getTotalStudents() ?: 0
    }
    
    /**
     * Пример сложной логики в Repository
     * 
     * Можно добавить:
     * - Кэширование
     * - Объединение данных из БД и API
     * - Валидацию перед сохранением
     * - Логирование операций
     */
    suspend fun addCourseWithValidation(course: Course): Result<Long> {
        return try {
            // Валидация
            if (course.title.isBlank()) {
                return Result.failure(Exception("Название курса не может быть пустым"))
            }
            if (course.price < 0) {
                return Result.failure(Exception("Цена не может быть отрицательной"))
            }
            if (course.rating !in 0.0f..5.0f) {
                return Result.failure(Exception("Рейтинг должен быть от 0 до 5"))
            }
            
            // Вставка в БД
            val id = courseDao.insert(course)
            Result.success(id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
