package com.example.coursedatabase.data.dao

import androidx.room.*
import com.example.coursedatabase.data.entity.Course
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)
    
    @Query("SELECT * FROM courses ORDER BY created_at DESC")
    fun getAllCourses(): Flow<List<Course>>
    
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?
    
    @Query("SELECT * FROM courses WHERE title LIKE '%' || :query || '%'")
    fun searchCourses(query: String): Flow<List<Course>>
    
    @Query("SELECT * FROM courses WHERE rating >= :minRating ORDER BY rating DESC")
    fun getCoursesByRating(minRating: Float): Flow<List<Course>>
    
    @Query("SELECT * FROM courses WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getCoursesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<Course>>
    
    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCoursesCount(): Int
    
    @Update
    suspend fun update(course: Course): Int
    
    @Query("UPDATE courses SET price = :newPrice WHERE id = :courseId")
    suspend fun updatePrice(courseId: Long, newPrice: Double)
    
    @Query("UPDATE courses SET rating = :rating, students_count = :studentsCount WHERE id = :courseId")
    suspend fun updateRatingAndStudents(courseId: Long, rating: Float, studentsCount: Int)
    
    @Delete
    suspend fun delete(course: Course): Int
    
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Long): Int
    
    @Query("DELETE FROM courses")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM courses ORDER BY students_count DESC LIMIT :limit")
    fun getTopCourses(limit: Int = 5): Flow<List<Course>>
    
    @Query("SELECT AVG(price) FROM courses")
    suspend fun getAveragePrice(): Double?
    
    @Query("SELECT SUM(students_count) FROM courses")
    suspend fun getTotalStudents(): Int?
}
