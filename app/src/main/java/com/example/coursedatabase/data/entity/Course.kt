package com.example.coursedatabase.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "instructor")
    val instructor: String,
    
    @ColumnInfo(name = "duration")
    val duration: String,
    
    @ColumnInfo(name = "price")
    val price: Double,
    
    @ColumnInfo(name = "rating")
    val rating: Float,
    
    @ColumnInfo(name = "students_count")
    val studentsCount: Int,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "firestore_id")
    val firestoreId: String? = null
) {
    companion object {
        fun getSampleCourses(): List<Course> {
            return listOf(
                Course(
                    title = "Android Development с Kotlin",
                    description = "Полный курс разработки Android приложений с нуля",
                    instructor = "Асхат Аманжолов",
                    duration = "40 часов",
                    price = 15990.0,
                    rating = 4.8f,
                    studentsCount = 2543
                )
            )
        }
    }
}
