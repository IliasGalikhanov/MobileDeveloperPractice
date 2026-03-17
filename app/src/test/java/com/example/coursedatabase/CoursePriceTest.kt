package com.example.coursedatabase

import com.example.coursedatabase.data.entity.Course
import org.junit.Assert.assertEquals
import org.junit.Test

class CoursePriceTest {

    @Test
    fun testCalculateDiscount() {
        val price = 10000.0
        val discount = 20
        val expected = 8000.0
        
        // Мы можем протестировать логику расчета напрямую
        val result = price * (1 - discount / 100.0)
        
        assertEquals(expected, result, 0.1)
    }

    @Test
    fun testCourseEntityMapping() {
        val course = Course(
            title = "Test",
            description = "Desc",
            instructor = "Inst",
            duration = "10h",
            price = 5000.0,
            rating = 4.5f,
            studentsCount = 100,
            firestoreId = "abc-123"
        )
        
        assertEquals("Test", course.title)
        assertEquals("abc-123", course.firestoreId)
    }
}
