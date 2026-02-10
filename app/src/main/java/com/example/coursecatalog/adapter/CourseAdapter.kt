package com.example.coursecatalog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecatalog.databinding.ItemCourseBinding
import com.example.coursecatalog.model.Course

class CourseAdapter(
    private val onCourseClick: (Course) -> Unit
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
                tvPrice.text = "${course.price.toInt()} тг"
                ratingBar.rating = course.rating
                tvRating.text = course.rating.toString()
                tvStudents.text = "${course.studentsCount} студентов"
                root.setOnClickListener {
                    onCourseClick(course)
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

    private class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem
        }
    }
}
