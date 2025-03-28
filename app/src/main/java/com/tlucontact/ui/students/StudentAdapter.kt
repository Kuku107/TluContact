package com.tlucontact.ui.students

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tlucontact.R
import com.tlucontact.data.models.StudentModel
import com.tlucontact.databinding.ItemStudentBinding

class StudentAdapter(private val onStudentClicked: (StudentModel) -> Unit) :
    ListAdapter<StudentModel, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudentViewHolder(private val binding: ItemStudentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onStudentClicked(getItem(position))
                }
            }
        }

        fun bind(student: StudentModel) {
            binding.tvName.text = student.name
            binding.tvStudentId.text = student.studentId
            binding.tvClass.text = student.className  // Đã đổi từ student.`class` thành student.className

            if (!student.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(student.avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<StudentModel>() {
        override fun areItemsTheSame(oldItem: StudentModel, newItem: StudentModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StudentModel, newItem: StudentModel): Boolean {
            return oldItem == newItem
        }
    }
}