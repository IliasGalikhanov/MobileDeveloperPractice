package com.example.coursedatabase

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursedatabase.adapter.CourseAdapter
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.databinding.ActivityMainBinding
import com.example.coursedatabase.databinding.DialogAddCourseBinding
import com.example.coursedatabase.viewmodel.CourseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * MainActivity с интеграцией Room Database
 * 
 * Демонстрирует полный CRUD цикл:
 * - CREATE: добавление курсов через диалог
 * - READ: отображение всех курсов из БД через LiveData
 * - UPDATE: редактирование существующих курсов
 * - DELETE: удаление курсов
 * 
 * Данные автоматически сохраняются в SQLite БД
 * и восстанавливаются после перезапуска приложения
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        println("MainActivity: onCreate - инициализация Room Database")
        
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    /**
     * Настройка RecyclerView с CourseAdapter
     */
    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(
            onCourseClick = { course ->
                showCourseDetails(course)
            },
            onCourseEdit = { course ->
                showEditCourseDialog(course)
            },
            onCourseDelete = { course ->
                showDeleteConfirmation(course)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = courseAdapter
            setHasFixedSize(true)
        }
    }

    /**
     * Настройка наблюдателей за LiveData
     * 
     * ВАЖНО: LiveData автоматически обновляется при изменениях в БД!
     * Room + LiveData = реактивное обновление UI
     */
    private fun setupObservers() {
        // Наблюдение за всеми курсами из БД
        viewModel.allCourses.observe(this) { courses ->
            println("MainActivity: Получены курсы из БД (размер: ${courses.size})")
            
            // submitList() + DiffUtil обновляет только изменения
            courseAdapter.submitList(courses)
            
            // Показ/скрытие пустого состояния
            binding.layoutEmpty.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (courses.isEmpty()) View.GONE else View.VISIBLE
        }
        
        // Наблюдение за статистикой
        viewModel.statistics.observe(this) { stats ->
            binding.tvCoursesCount.text = stats.coursesCount.toString()
            binding.tvAveragePrice.text = "${stats.averagePrice.toInt()} ₽"
            binding.tvTotalStudents.text = stats.totalStudents.toString()
            
            println("MainActivity: Статистика обновлена - курсов: ${stats.coursesCount}")
        }
        
        // Наблюдение за ошибками
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showSnackbar(it)
                viewModel.clearError()
            }
        }
    }

    /**
     * Настройка обработчиков кликов
     */
    private fun setupClickListeners() {
        binding.fabAddCourse.setOnClickListener {
            showAddCourseDialog()
        }
    }

    /**
     * Диалог добавления курса в БД
     */
    private fun showAddCourseDialog() {
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val course = createCourseFromDialog(dialogBinding)
                if (course != null) {
                    // Вставка в БД через ViewModel
                    viewModel.insertWithValidation(
                        course,
                        onSuccess = { id ->
                            showSnackbar("${getString(R.string.course_added)} (ID: $id)")
                        },
                        onError = { error ->
                            showSnackbar(error)
                        }
                    )
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Диалог редактирования курса в БД
     */
    private fun showEditCourseDialog(course: Course) {
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)
        
        // Заполняем текущими значениями
        dialogBinding.apply {
            etTitle.setText(course.title)
            etDescription.setText(course.description)
            etInstructor.setText(course.instructor)
            etDuration.setText(course.duration)
            etPrice.setText(course.price.toString())
            etRating.setText(course.rating.toString())
            etStudents.setText(course.studentsCount.toString())
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.edit_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val updatedCourse = createCourseFromDialog(dialogBinding, course.id)
                if (updatedCourse != null) {
                    // Обновление в БД
                    viewModel.update(updatedCourse)
                    showSnackbar(getString(R.string.course_updated))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Подтверждение удаления из БД
     */
    private fun showDeleteConfirmation(course: Course) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_course)
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton(R.string.delete) { _, _ ->
                // Удаление из БД
                viewModel.delete(course)
                showSnackbar(getString(R.string.course_deleted))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Детальная информация о курсе
     */
    private fun showCourseDetails(course: Course) {
        val message = """
            Описание: ${course.description}
            Преподаватель: ${course.instructor}
            Длительность: ${course.duration}
            Цена: ${course.price.toInt()} ₽
            Рейтинг: ${course.rating}
            Студентов: ${course.studentsCount}
            
            ID в БД: ${course.id}
            Создан: ${java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault()).format(course.createdAt)}
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle(course.title)
            .setMessage(message)
            .setPositiveButton(R.string.enroll) { _, _ ->
                // "Запись на курс" обновляет данные в БД
                viewModel.enrollToCourse(course.id, course.studentsCount, course.rating)
                showSnackbar(getString(R.string.enrolled_success))
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    /**
     * Создание объекта Course из диалога
     */
    private fun createCourseFromDialog(
        binding: DialogAddCourseBinding,
        existingId: Long = 0L  // 0 для нового курса, Room автоматически сгенерирует ID
    ): Course? {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val instructor = binding.etInstructor.text.toString().trim()
        val duration = binding.etDuration.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val ratingStr = binding.etRating.text.toString().trim()
        val studentsStr = binding.etStudents.text.toString().trim()

        // Простая валидация
        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.error_empty_title)
            return null
        }

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val rating = ratingStr.toFloatOrNull() ?: 0.0f
        val students = studentsStr.toIntOrNull() ?: 0

        return Course(
            id = existingId,
            title = title,
            description = description.ifEmpty { "Описание отсутствует" },
            instructor = instructor.ifEmpty { "Не указан" },
            duration = duration.ifEmpty { "Не указана" },
            price = price,
            rating = rating.coerceIn(0f, 5f),
            studentsCount = students
        )
    }

    /**
     * Показ Snackbar
     */
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
