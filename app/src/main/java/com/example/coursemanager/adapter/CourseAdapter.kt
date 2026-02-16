package com.example.coursemanager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coursemanager.databinding.ItemCourseBinding
import com.example.coursemanager.model.Course
import java.text.DecimalFormat

class CourseAdapter(
    private val onCourseClick: (Course) -> Unit,
    private val onCourseEdit: (Course) -> Unit,
    private val onCourseDelete: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val formatter = DecimalFormat("#,###")

        fun bind(course: Course) {
            binding.apply {
                tvCourseTitle.text = course.title
                tvCourseDescription.text = course.description
                tvInstructor.text = "Преподаватель: ${course.instructor}"
                tvDuration.text = course.duration
                tvPrice.text = "${formatter.format(course.price)} ₸"
                ratingBar.rating = course.rating
                tvRating.text = course.rating.toString()
                tvStudents.text = "(${course.studentsCount})"

                root.setOnClickListener { onCourseClick(course) }
                btnEdit.setOnClickListener { onCourseEdit(course) }
                btnDelete.setOnClickListener { onCourseDelete(course) }
            }
        }
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
