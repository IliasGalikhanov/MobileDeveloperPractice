package com.example.coursedatabase.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.coursedatabase.data.dao.CourseDao
import com.example.coursedatabase.data.entity.Course
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Главный класс базы данных Room
 * 
 * @Database - аннотация Room для определения БД
 * entities = [Course::class] - список Entity классов (таблиц) в БД
 * version = 1 - версия схемы БД (увеличивается при изменениях)
 * exportSchema = false - отключает экспорт схемы БД (для простоты)
 * 
 * RoomDatabase - абстрактный класс Room, предоставляющий функционал БД
 */
@Database(
    entities = [Course::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Абстрактный метод для получения DAO
     * Room автоматически генерирует реализацию
     */
    abstract fun courseDao(): CourseDao
    
    companion object {
        /**
         * Singleton instance базы данных
         * @Volatile - гарантирует видимость изменений между потоками
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Получение экземпляра базы данных
         * 
         * Использует паттерн Singleton с Double-Checked Locking
         * для потокобезопасного создания единственного экземпляра БД
         * 
         * @param context - контекст приложения
         * @return AppDatabase - единственный экземпляр БД
         */
        fun getDatabase(context: Context): AppDatabase {
            // Первая проверка (без блокировки)
            return INSTANCE ?: synchronized(this) {
                // Вторая проверка (с блокировкой)
                val instance = INSTANCE ?: buildDatabase(context).also {
                    INSTANCE = it
                }
                instance
            }
        }
        
        /**
         * Создание экземпляра базы данных
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "course_database"  // Имя файла БД
            )
                // Добавляем callback для первоначального заполнения
                .addCallback(DatabaseCallback())
                // .fallbackToDestructiveMigration() - удаляет БД при изменении версии
                // Используется только для разработки!
                .build()
        }
        
        /**
         * Callback для первоначального заполнения БД демо-данными
         * 
         * Вызывается один раз при создании БД
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // Заполняем БД демо-данными в фоновом потоке
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.courseDao())
                    }
                }
            }
            
            /**
             * Заполнение БД демонстрационными курсами
             */
            suspend fun populateDatabase(courseDao: CourseDao) {
                // Очистка БД (если нужно)
                // courseDao.deleteAll()
                
                // Вставка демо-курсов
                val sampleCourses = Course.getSampleCourses()
                courseDao.insertAll(sampleCourses)
                
                println("AppDatabase: База данных заполнена ${sampleCourses.size} курсами")
            }
        }
        
        /**
         * Очистка instance (для тестирования)
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
