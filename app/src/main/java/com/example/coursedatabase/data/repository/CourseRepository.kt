package com.example.coursedatabase.data.repository

import android.util.Log
import com.example.coursedatabase.data.dao.CourseDao
import com.example.coursedatabase.data.dto.PostDto
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.data.network.ApiService
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val courseDao: CourseDao,
    private val apiService: ApiService
) {

    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()

    suspend fun fetchPosts(): List<PostDto> {
        return apiService.getPosts()
    }

    suspend fun insert(course: Course): Long {
        Log.d("CourseRepository", "Inserting course: $course")
        return courseDao.insert(course)
    }

    suspend fun insertAll(courses: List<Course>) {
        Log.d("CourseRepository", "Inserting all courses")
        courseDao.insertAll(courses)
    }

    suspend fun getCourseById(id: Long): Course? {
        Log.d("CourseRepository", "Getting course by id: $id")
        return courseDao.getCourseById(id)
    }

    fun searchCourses(query: String): Flow<List<Course>> {
        Log.d("CourseRepository", "Searching courses with query: $query")
        return courseDao.searchCourses(query)
    }

    fun getCoursesByRating(minRating: Float): Flow<List<Course>> {
        Log.d("CourseRepository", "Getting courses by rating: $minRating")
        return courseDao.getCoursesByRating(minRating)
    }

    fun getCoursesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Course>> {
        Log.d("CourseRepository", "Getting courses by price range: $minPrice - $maxPrice")
        return courseDao.getCoursesByPriceRange(minPrice, maxPrice)
    }

    suspend fun getCoursesCount(): Int {
        Log.d("CourseRepository", "Getting courses count")
        return courseDao.getCoursesCount()
    }

    suspend fun update(course: Course): Int {
        Log.d("CourseRepository", "Updating course: $course")
        return courseDao.update(course)
    }

    suspend fun updatePrice(courseId: Long, newPrice: Double) {
        Log.d("CourseRepository", "Updating price for course: $courseId")
        courseDao.updatePrice(courseId, newPrice)
    }

    suspend fun updateRatingAndStudents(courseId: Long, rating: Float, studentsCount: Int) {
        Log.d("CourseRepository", "Updating rating and students for course: $courseId")
        courseDao.updateRatingAndStudents(courseId, rating, studentsCount)
    }

    suspend fun delete(course: Course): Int {
        Log.d("CourseRepository", "Deleting course: $course")
        return courseDao.delete(course)
    }

    suspend fun deleteById(courseId: Long): Int {
        Log.d("CourseRepository", "Deleting course by id: $courseId")
        courseDao.deleteById(courseId)
    }

    suspend fun deleteAll() {
        Log.d("CourseRepository", "Deleting all courses")
        courseDao.deleteAll()
    }

    fun getTopCourses(limit: Int = 5): Flow<List<Course>> {
        Log.d("CourseRepository", "Getting top courses")
        return courseDao.getTopCourses(limit)
    }

    suspend fun getAveragePrice(): Double {
        Log.d("CourseRepository", "Getting average price")
        return courseDao.getAveragePrice() ?: 0.0
    }

    suspend fun getTotalStudents(): Int {
        Log.d("CourseRepository", "Getting total students")
        return courseDao.getTotalStudents() ?: 0
    }

    suspend fun addCourseWithValidation(course: Course): Result<Long> {
        Log.d("CourseRepository", "Adding course with validation: $course")
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
