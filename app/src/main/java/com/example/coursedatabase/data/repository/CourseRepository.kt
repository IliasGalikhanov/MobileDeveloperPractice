package com.example.coursedatabase.data.repository

import androidx.lifecycle.LiveData
import com.example.coursedatabase.data.dao.CourseDao
import com.example.coursedatabase.data.entity.Course

class CourseRepository(private val courseDao: CourseDao) {
    
    val allCourses: LiveData<List<Course>> = courseDao.getAllCourses()
    
    suspend fun insert(course: Course): Long {
        return courseDao.insert(course)
    }
    
    suspend fun insertAll(courses: List<Course>) {
        courseDao.insertAll(courses)
    }
    
    suspend fun getCourseById(id: Long): Course? {
        return courseDao.getCourseById(id)
    }
    
    fun searchCourses(query: String): LiveData<List<Course>> {
        return courseDao.searchCourses(query)
    }
    
    fun getCoursesByRating(minRating: Float): LiveData<List<Course>> {
        return courseDao.getCoursesByRating(minRating)
    }
    
    fun getCoursesByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Course>> {
        return courseDao.getCoursesByPriceRange(minPrice, maxPrice)
    }
    
    suspend fun getCoursesCount(): Int {
        return courseDao.getCoursesCount()
    }
    
    suspend fun update(course: Course): Int {
        return courseDao.update(course)
    }
    
    suspend fun updatePrice(courseId: Long, newPrice: Double) {
        courseDao.updatePrice(courseId, newPrice)
    }
    
    suspend fun updateRatingAndStudents(courseId: Long, rating: Float, studentsCount: Int) {
        courseDao.updateRatingAndStudents(courseId, rating, studentsCount)
    }
    
    suspend fun delete(course: Course): Int {
        return courseDao.delete(course)
    }
    
    suspend fun deleteById(courseId: Long): Int {
        return courseDao.deleteById(courseId)
    }
    
    suspend fun deleteAll() {
        courseDao.deleteAll()
    }
    
    fun getTopCourses(limit: Int = 5): LiveData<List<Course>> {
        return courseDao.getTopCourses(limit)
    }
    
    suspend fun getAveragePrice(): Double {
        return courseDao.getAveragePrice() ?: 0.0
    }
    
    suspend fun getTotalStudents(): Int {
        return courseDao.getTotalStudents() ?: 0
    }
    
    suspend fun addCourseWithValidation(course: Course): Result<Long> {
        return try {
            if (course.title.isBlank()) {
                return Result.failure(Exception("Название курса не может быть пустым"))
            }
            if (course.price < 0) {
                return Result.failure(Exception("Цена не может быть отрицательной"))
            }
            if (course.rating !in 0.0f..5.0f) {
                return Result.failure(Exception("Рейтинг должен быть от 0 до 5"))
            }
            
            val id = courseDao.insert(course)
            Result.success(id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
