package com.example.coursedatabase

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coursedatabase.adapter.CourseAdapter
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.data.network.RetrofitProvider
import com.example.coursedatabase.databinding.ActivityMainBinding
import com.example.coursedatabase.databinding.DialogAddCourseBinding
import com.example.coursedatabase.viewmodel.AuthViewModel
import com.example.coursedatabase.viewmodel.CourseViewModel
import com.example.coursedatabase.viewmodel.CourseViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CourseViewModel by viewModels {
        val database = com.example.coursedatabase.data.database.AppDatabase.getDatabase(applicationContext)
        val apiService = RetrofitProvider.apiService
        val repository = com.example.coursedatabase.data.repository.CourseRepository(database.courseDao(), apiService)
        CourseViewModelFactory(repository)
    }
    private val authViewModel: AuthViewModel by viewModels()
    
    private lateinit var courseAdapter: CourseAdapter
    private var userRole: String = "student"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userRole = authViewModel.getUserRole(this)
        
        setupUIBasedOnRole()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUIBasedOnRole() {
        if (userRole == "teacher") {
            binding.fabAddCourse.visibility = View.VISIBLE
            binding.toolbar.title = "Панель Учителя"
            binding.tvCoursesHeader.text = "📚 Все курсы (Firestore/Room)"
        } else {
            binding.fabAddCourse.visibility = View.GONE
            binding.toolbar.title = "Панель Ученика"
            binding.tvCoursesHeader.text = "📚 Доступные курсы (Firestore)"
        }
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(
            onCourseClick = { course ->
                showCourseDetails(course)
            },
            onCourseEdit = { course ->
                if (userRole == "teacher") showEditCourseDialog(course)
                else showSnackbar("Только учителя могут редактировать курсы")
            },
            onCourseDelete = { course ->
                if (userRole == "teacher") showDeleteConfirmation(course)
                else showSnackbar("Только учителя могут удалять курсы")
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = courseAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.allCourses.observe(this) { courses ->
            courseAdapter.submitList(courses)
            binding.layoutEmpty.visibility = if (courses.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (courses.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.statistics.observe(this) { stats ->
            binding.tvCoursesCount.text = stats.coursesCount.toString()
            binding.tvAveragePrice.text = "${stats.averagePrice.toInt()} ₸"
            binding.tvTotalStudents.text = stats.totalStudents.toString()
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                showSnackbar(it)
                viewModel.clearError()
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.btnRefresh.isEnabled = false
            } else {
                binding.btnRefresh.isEnabled = true
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddCourse.setOnClickListener {
            showAddCourseDialog()
        }
        binding.btnRefresh.setOnClickListener {
            viewModel.fetchCoursesFromFirestore()
            showSnackbar("Обновление данных...")
        }
        
        binding.btnAccount.setOnClickListener {
            showAccountDialog()
        }
    }

    private fun showAccountDialog() {
        val email = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "Неизвестно"
        val roleDisplay = if (userRole == "teacher") "Учитель" else "Ученик"
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Ваш профиль")
            .setMessage("Email: $email\nРоль: $roleDisplay")
            .setPositiveButton("Выйти") { _, _ ->
                authViewModel.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Закрыть", null)
            .show()
    }

    private fun showAddCourseDialog() {
        if (userRole != "teacher") return
        
        val dialogBinding = DialogAddCourseBinding.inflate(layoutInflater)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_course)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val course = createCourseFromDialog(dialogBinding)
                if (course != null) {
                    viewModel.insert(course)
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
                // ПЕРЕДАЕМ ОБЪЕКТ course целиком, чтобы сохранить firestoreId
                val updatedCourse = createCourseFromDialog(dialogBinding, course)
                if (updatedCourse != null) {
                    viewModel.update(updatedCourse)
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
                viewModel.delete(course)
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
            Цена: ${course.price.toInt()} ₸
            Рейтинг: ${course.rating}
            Студентов: ${course.studentsCount}
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle(course.title)
            .setMessage(message)
            .setPositiveButton(if (userRole == "student") R.string.enroll else R.string.close) { _, _ ->
                if (userRole == "student") {
                    viewModel.enrollToCourse(course.id, course.studentsCount, course.rating)
                    showSnackbar(getString(R.string.enrolled_success))
                }
            }
            .apply {
                if (userRole == "student") setNegativeButton(R.string.close, null)
            }
            .show()
    }

    private fun createCourseFromDialog(
        binding: DialogAddCourseBinding,
        existingCourse: Course? = null
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

        val price = priceStr.toDoubleOrNull() ?: 0.0
        val rating = ratingStr.toFloatOrNull() ?: 0.0f
        val students = studentsStr.toIntOrNull() ?: 0

        return Course(
            id = existingCourse?.id ?: 0L,
            title = title,
            description = description.ifEmpty { "Описание отсутствует" },
            instructor = instructor.ifEmpty { "Не указан" },
            duration = duration.ifEmpty { "Не указана" },
            price = price,
            rating = rating.coerceIn(0f, 5f),
            studentsCount = students,
            createdAt = existingCourse?.createdAt ?: System.currentTimeMillis(),
            firestoreId = existingCourse?.firestoreId // Передаем ID из Firestore!
        )
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}
