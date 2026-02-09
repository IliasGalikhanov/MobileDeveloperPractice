package com.example.coursecatalog.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coursecatalog.databinding.ItemCourseBinding
import com.example.coursecatalog.model.Course

/**
 * Adapter для отображения списка курсов в RecyclerView
 * 
 * Использует ListAdapter с DiffUtil для эффективного обновления списка
 * 
 * @param onCourseClick callback для обработки клика по курсу
 */
class CourseAdapter(
    private val onCourseClick: (Course) -> Unit
) : ListAdapter<Course, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    /**
     * ViewHolder для элемента списка курсов
     * 
     * ViewHolder паттерн позволяет переиспользовать view элементы,
     * что значительно улучшает производительность RecyclerView
     */
    inner class CourseViewHolder(
        private val binding: ItemCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Привязка данных курса к элементам UI
         */
        fun bind(course: Course) {
            binding.apply {
                // Основная информация
                tvCourseTitle.text = course.title
                tvCourseDescription.text = course.description
                tvInstructor.text = "Преподаватель: ${course.instructor}"
                tvDuration.text = course.duration
                
                // Цена
                tvPrice.text = "${course.price.toInt()} тг"
                
                // Рейтинг
                ratingBar.rating = course.rating
                tvRating.text = course.rating.toString()
                
                // Количество студентов
                tvStudents.text = "${course.studentsCount} студентов"
                
                // Обработка клика на весь элемент
                root.setOnClickListener {
                    onCourseClick(course)
                }
            }
        }
    }

    /**
     * Создание ViewHolder
     * Вызывается когда RecyclerView нужен новый ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    /**
     * Привязка данных к ViewHolder
     * Вызывается для отображения данных на позиции position
     */
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = getItem(position)
        holder.bind(course)
    }

    /**
     * DiffUtil.ItemCallback для эффективного сравнения элементов списка
     * 
     * DiffUtil вычисляет минимальное количество изменений между двумя списками
     * и обновляет только изменившиеся элементы
     */
    private class CourseDiffCallback : DiffUtil.ItemCallback<Course>() {
        /**
         * Проверка, являются ли элементы одним и тем же объектом
         * Обычно сравнивается по ID
         */
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem.id == newItem.id
        }

        /**
         * Проверка, изменилось ли содержимое элемента
         * Вызывается только если areItemsTheSame вернул true
         */
        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean {
            return oldItem == newItem
        }
    }
}
