package com.example.courseevents.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.courseevents.databinding.ItemCourseBinding
import com.example.courseevents.model.Course

class CourseAdapter(private var listCourses: ArrayList<Course>): RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {
    private var onItemClickCallback: OnItemClickCallback? = null

    fun setOnItemClickCallback (onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(listCourses[position])
    }

    override fun getItemCount(): Int = listCourses.size

    fun searchDataList(searchList: ArrayList<Course>) {
        listCourses = searchList
        notifyDataSetChanged()
    }

    inner class CourseViewHolder(private val binding: ItemCourseBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(course: Course) {
            binding.tvItemName.text = course.name
            binding.tvItemDay.text = course.day
            binding.tvItemStartTime.text = course.startTime
            binding.tvItemEndTime.text = course.endTime

            binding.root.setOnClickListener{
                onItemClickCallback?.onItemClicked(course)
            }
        }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: Course)
    }
}
