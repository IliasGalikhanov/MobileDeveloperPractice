package com.example.coursedatabase.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.coursedatabase.data.entity.Course

/**
 * Data Access Object (DAO) для работы с таблицей courses
 * 
 * @Dao - аннотация Room, указывающая что это интерфейс для доступа к данным
 * 
 * DAO определяет методы для работы с БД:
 * - CRUD операции (Create, Read, Update, Delete)
 * - Запросы к БД
 * 
 * Room автоматически генерирует реализацию этого интерфейса
 */
@Dao
interface CourseDao {
    
    /**
     * CREATE - Вставка одного курса
     * 
     * @Insert - Room автоматически генерирует SQL INSERT
     * onConflict = OnConflictStrategy.REPLACE - при конфликте ID заменяет запись
     * 
     * suspend - корутина, выполняется асинхронно в фоновом потоке
     * 
     * @return Long - ID вставленной записи
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long
    
    /**
     * CREATE - Вставка списка курсов
     * 
     * Используется для первоначального заполнения БД
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<Course>)
    
    /**
     * READ - Получение всех курсов
     * 
     * @Query - SQL запрос
     * LiveData - реактивный тип данных, автоматически обновляется при изменении БД
     * 
     * Когда данные в таблице courses меняются,
     * LiveData автоматически уведомляет подписчиков
     * 
     * @return LiveData<List<Course>> - список всех курсов, обновляется автоматически
     */
    @Query("SELECT * FROM courses ORDER BY created_at DESC")
    fun getAllCourses(): LiveData<List<Course>>
    
    /**
     * READ - Получение курса по ID
     * 
     * :id - параметр запроса, Room автоматически подставляет значение
     */
    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): Course?
    
    /**
     * READ - Поиск курсов по названию
     * 
     * LIKE - SQL оператор для поиска по шаблону
     * '%' || :query || '%' - поиск подстроки в любой части названия
     */
    @Query("SELECT * FROM courses WHERE title LIKE '%' || :query || '%'")
    fun searchCourses(query: String): LiveData<List<Course>>
    
    /**
     * READ - Фильтрация по рейтингу
     */
    @Query("SELECT * FROM courses WHERE rating >= :minRating ORDER BY rating DESC")
    fun getCoursesByRating(minRating: Float): LiveData<List<Course>>
    
    /**
     * READ - Фильтрация по ценовому диапазону
     */
    @Query("SELECT * FROM courses WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getCoursesByPriceRange(minPrice: Double, maxPrice: Double): LiveData<List<Course>>
    
    /**
     * READ - Получение количества курсов
     */
    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCoursesCount(): Int
    
    /**
     * UPDATE - Обновление курса
     * 
     * @Update - Room генерирует SQL UPDATE
     * Обновляет запись с соответствующим ID
     * 
     * @return Int - количество обновленных записей (обычно 1 или 0)
     */
    @Update
    suspend fun update(course: Course): Int
    
    /**
     * UPDATE - Обновление цены курса
     * 
     * Пример точечного обновления одного поля
     */
    @Query("UPDATE courses SET price = :newPrice WHERE id = :courseId")
    suspend fun updatePrice(courseId: Long, newPrice: Double)
    
    /**
     * UPDATE - Обновление рейтинга и количества студентов
     */
    @Query("UPDATE courses SET rating = :rating, students_count = :studentsCount WHERE id = :courseId")
    suspend fun updateRatingAndStudents(courseId: Long, rating: Float, studentsCount: Int)
    
    /**
     * DELETE - Удаление курса
     * 
     * @Delete - Room генерирует SQL DELETE
     * Удаляет запись с ID из переданного объекта
     * 
     * @return Int - количество удаленных записей
     */
    @Delete
    suspend fun delete(course: Course): Int
    
    /**
     * DELETE - Удаление курса по ID
     */
    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteById(courseId: Long): Int
    
    /**
     * DELETE - Удаление всех курсов
     * 
     * Используется для очистки БД (например, в тестах)
     */
    @Query("DELETE FROM courses")
    suspend fun deleteAll()
    
    /**
     * CUSTOM - Получение самых популярных курсов
     * 
     * Комбинированный запрос с сортировкой и лимитом
     */
    @Query("SELECT * FROM courses ORDER BY students_count DESC LIMIT :limit")
    fun getTopCourses(limit: Int = 5): LiveData<List<Course>>
    
    /**
     * CUSTOM - Получение средней цены всех курсов
     */
    @Query("SELECT AVG(price) FROM courses")
    suspend fun getAveragePrice(): Double?
    
    /**
     * CUSTOM - Получение общего количества студентов
     */
    @Query("SELECT SUM(students_count) FROM courses")
    suspend fun getTotalStudents(): Int?
}
