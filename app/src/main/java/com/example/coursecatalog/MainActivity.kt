package com.example.coursecatalog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursecatalog.adapter.CourseAdapter
import com.example.coursecatalog.databinding.ActivityMainBinding
import com.example.coursecatalog.model.Course
import com.example.coursecatalog.viewmodel.CourseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Главная Activity приложения с каталогом курсов
 * Демонстрирует работу RecyclerView, Adapter и ViewHolder
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFilters()
    }

    /**
     * Настройка Toolbar
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Каталог курсов"
        }
    }

    /**
     * Настройка RecyclerView
     */
    private fun setupRecyclerView() {
        // Создание адаптера с обработчиком клика
        courseAdapter = CourseAdapter { course ->
            onCourseClick(course)
        }

        binding.recyclerView.apply {
            // Установка LayoutManager
            // LinearLayoutManager отображает элементы вертикально
            layoutManager = LinearLayoutManager(this@MainActivity)
            
            // Установка адаптера
            adapter = courseAdapter
            
            // Оптимизация производительности
            // Используется когда размер RecyclerView не зависит от содержимого
            setHasFixedSize(true)
            
            // Добавление разделителей между элементами (опционально)
            // addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    /**
     * Подписка на LiveData из ViewModel
     */
    private fun setupObservers() {
        // Наблюдение за списком курсов
        viewModel.courses.observe(this) { courses ->
            // Обновление списка в адаптере
            courseAdapter.submitList(courses)
            
            // Обновление счётчика курсов
            binding.tvCoursesCount.text = "Найдено курсов: ${courses.size}"
        }

        // Наблюдение за выбранным курсом
        viewModel.selectedCourse.observe(this) { course ->
            course?.let {
                showCourseDetails(it)
                viewModel.clearSelection()
            }
        }
    }

    /**
     * Настройка фильтров и сортировки
     */
    private fun setupFilters() {
        binding.apply {
            // Кнопка сортировки по цене
            btnSortPrice.setOnClickListener {
                showSortDialog()
            }

            // Кнопка фильтрации по рейтингу
            btnFilterRating.setOnClickListener {
                showFilterDialog()
            }

            // Кнопка сброса фильтров
            btnResetFilters.setOnClickListener {
                viewModel.resetFilters()
                Toast.makeText(
                    this@MainActivity,
                    "Фильтры сброшены",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Обработка клика по курсу
     */
    private fun onCourseClick(course: Course) {
        viewModel.selectCourse(course)
    }

    /**
     * Отображение детальной информации о курсе
     */
    private fun showCourseDetails(course: Course) {
        val message = """
            📚 ${course.title}
            
            👨‍🏫 ${course.instructor}
            ⏱️ Длительность: ${course.duration}
            💰 Цена: ${course.price.toInt()} тг
            ⭐ Рейтинг: ${course.rating}/5.0
            👥 Студентов: ${course.studentsCount}
            
            ${course.description}
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("Информация о курсе")
            .setMessage(message)
            .setPositiveButton("Записаться") { _, _ ->
                Toast.makeText(
                    this,
                    "Вы записались на курс: ${course.title}",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    /**
     * Диалог выбора сортировки
     */
    private fun showSortDialog() {
        val options = arrayOf(
            "По возрастанию цены",
            "По убыванию цены"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Сортировка")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.sortByPrice(ascending = true)
                        Toast.makeText(this, "Сортировка по возрастанию", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        viewModel.sortByPrice(ascending = false)
                        Toast.makeText(this, "Сортировка по убыванию", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }

    /**
     * Диалог выбора фильтра по рейтингу
     */
    private fun showFilterDialog() {
        val options = arrayOf(
            "Все курсы",
            "Рейтинг 4.5+",
            "Рейтинг 4.7+",
            "Рейтинг 4.8+"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Фильтр по рейтингу")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        viewModel.resetFilters()
                        Toast.makeText(this, "Показаны все курсы", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        viewModel.filterByRating(4.5f)
                        Toast.makeText(this, "Фильтр: рейтинг 4.5+", Toast.LENGTH_SHORT).show()
                    }
                    2 -> {
                        viewModel.filterByRating(4.7f)
                        Toast.makeText(this, "Фильтр: рейтинг 4.7+", Toast.LENGTH_SHORT).show()
                    }
                    3 -> {
                        viewModel.filterByRating(4.8f)
                        Toast.makeText(this, "Фильтр: рейтинг 4.8+", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .show()
    }
}
