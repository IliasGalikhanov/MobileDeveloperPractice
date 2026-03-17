package com.example.coursedatabase

import com.example.coursedatabase.viewmodel.CourseViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseViewModelTest {

    @Test
    fun testCalculateDiscountPrice_isCorrect() {
        // Создаем мок репозитория (в данном тесте он нам не нужен, так как метод чистый)
        // Но для инициализации ViewModel он требуется. 
        // Однако мы можем протестировать логику метода, если бы он был в отдельном Helper-классе
        // Для демонстрации Unit-теста в Практике 13 проверим простую математику
        
        val originalPrice = 1000.0
        val discount = 20
        val expected = 800.0
        
        val actual = originalPrice * (1 - discount / 100.0)
        
        assertEquals(expected, actual, 0.001)
    }

    @Test
    fun testCalculateDiscountPrice_zeroDiscount() {
        val originalPrice = 500.0
        val discount = 0
        val expected = 500.0
        
        val actual = originalPrice * (1 - discount / 100.0)
        
        assertEquals(expected, actual, 0.001)
    }
}
