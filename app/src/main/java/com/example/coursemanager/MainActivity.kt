package com.example.coursemanager

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursemanager.adapter.CourseAdapter
import com.example.coursemanager.databinding.ActivityMainBinding
import com.example.coursemanager.databinding.DialogAddCourseBinding
import com.example.coursemanager.model.Course
import com.example.coursemanager.viewmodel.CourseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CourseViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter
    private val formatter = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

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

    private fun setupObservers() {
        viewModel.courses.observe(this) { courses ->
            println("MainActivity: Получен новый список курсов (размер: ${courses.size})")

            courseAdapter.submitList(courses)

            binding.tvCoursesCount.text = getString(R.string.courses_count, courses.size)

            binding.layoutEmpty.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (courses.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.selectedCourse.observe(this) { course ->
            course?.let {

            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCourse.setOnClickListener {
            showAddCourseDialog()
        }

        binding.btnSortPrice.setOnClickListener {
            showSortDialog()
        }

        binding.btnFilterRating.setOnClickListener {
            showFilterDialog()
        }

        binding.btnResetFilters.setOnClickListener {
            viewModel.resetFilters()
            showSnackbar("Фильтры сброшены")
        }
    }

    private fun showAddCourseDialog() {
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)
        
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val course = createCourseFromDialog(dialogBinding)
                if (course != null) {
                    println("MainActivity: Добавление нового курса: ${course.title}")
                    viewModel.addCourse(course)
                    showSnackbar(getString(R.string.course_added))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditCourseDialog(course: Course) {
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)

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
                    println("MainActivity: Обновление курса: ${updatedCourse.title}")
                    viewModel.updateCourse(updatedCourse)
                    showSnackbar(getString(R.string.course_updated))
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteConfirmation(course: Course) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_course)
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton(R.string.delete) { _, _ ->
                println("MainActivity: Удаление курса: ${course.title}")
                viewModel.deleteCourse(course.id)
                showSnackbar(getString(R.string.course_deleted))
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showCourseDetails(course: Course) {
        val message = """
            Описание: ${course.description}
            Преподаватель: ${course.instructor}
            Длительность: ${course.duration}
            Цена: ${formatter.format(course.price)} ₸
            Рейтинг: ${course.rating}
            Студентов: ${course.studentsCount}
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle(course.title)
            .setMessage(message)
            .setPositiveButton(R.string.enroll) { _, _ ->
                val newRating = (course.rating + 0.1f).coerceAtMost(5.0f)
                viewModel.updateCourseRating(course.id, newRating)
                showSnackbar(getString(R.string.enrolled_success))
            }
            .setNegativeButton(R.string.close, null)
            .show()
    }

    private fun showSortDialog() {
        val options = arrayOf(
            getString(R.string.sort_price_asc),
            getString(R.string.sort_price_desc)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.sort_by_price)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.sortByPrice(ascending = true)
                    1 -> viewModel.sortByPrice(ascending = false)
                }
            }
            .show()
    }

    private fun showFilterDialog() {
        val options = arrayOf(
            getString(R.string.rating_45_plus),
            getString(R.string.rating_47_plus),
            getString(R.string.rating_48_plus)
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.filter_by_rating)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> viewModel.filterByRating(4.5f)
                    1 -> viewModel.filterByRating(4.7f)
                    2 -> viewModel.filterByRating(4.8f)
                }
            }
            .show()
    }

    private fun createCourseFromDialog(
        binding: DialogAddCourseBinding,
        existingId: String? = null
    ): Course? {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val instructor = binding.etInstructor.text.toString().trim()
        val duration = binding.etDuration.text.toString().trim()
        val priceStr = binding.etPrice.text.toString().trim()
        val ratingStr = binding.etRating.text.toString().trim()
        val studentsStr = binding.etStudents.text.toString().trim()

        if (title.isEmpty()) {
            binding.tilTitle.error = getString(R.string.error_empty_title)
            return null
        }
        if (description.isEmpty()) {
            binding.tilDescription.error = getString(R.string.error_empty_description)
            return null
        }
        if (instructor.isEmpty()) {
            binding.tilInstructor.error = getString(R.string.error_empty_instructor)
            return null
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.tilPrice.error = getString(R.string.error_invalid_price)
            return null
        }

        val rating = ratingStr.toFloatOrNull()
        if (rating == null || rating !in 0.0f..5.0f) {
            binding.tilRating.error = getString(R.string.error_invalid_rating)
            return null
        }

        val students = studentsStr.toIntOrNull()
        if (students == null || students < 0) {
            binding.tilStudents.error = getString(R.string.error_invalid_students)
            return null
        }

        return Course(
            id = existingId ?: java.util.UUID.randomUUID().toString(),
            title = title,
            description = description,
            instructor = instructor,
            duration = duration,
            price = price,
            rating = rating,
            studentsCount = students
        )
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
