package com.example.coursedatabase.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coursedatabase.data.entity.Course
import com.example.coursedatabase.databinding.ItemCourseBinding

/**
 * Adapter для отображения курсов из Room Database
 * 
 * ListAdapter + DiffUtil автоматически обновляют список
 * при изменениях в БД
 */
class CourseAdapter(
    private val onCourseClick: (Course) -> Unit,
    private val onCourseEdit: (Course) -> Unit,
    private val onCourseDelete: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.apply {
                tvCourseTitle.text = course.title
                tvCourseDescription.text = course.description
                tvInstructor.text = "Преподаватель: ${course.instructor}"
                tvDuration.text = course.duration
                tvPrice.text = "${course.price.toInt()} ₽"
                ratingBar.rating = course.rating
                tvRating.text = course.rating.toString()
                tvStudents.text = "${course.studentsCount} студентов"
                
                // ID курса для отладки
                tvCourseId.text = "ID: ${course.id}"
                
                root.setOnClickListener {
                    onCourseClick(course)
                }
                
                btnEdit.setOnClickListener {
                    onCourseEdit(course)
                }
                
                btnDelete.setOnClickListener {
                    onCourseDelete(course)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = getItem(position)
        holder.bind(course)
    }

    /**
     * DiffUtil для сравнения курсов из Room
     * 
     * ВАЖНО: Сравниваем по ID (Long), а не String
     * Room автоматически генерирует Long ID
     */
    private class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            // Сравниваем по ID из Room (Long)
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            // Data class автоматически сравнивает все поля
            return oldItem == newItem
        }
    }
}
