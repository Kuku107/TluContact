package com.tlucontact.ui.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tlucontact.R
import com.tlucontact.data.models.StaffModel
import com.tlucontact.databinding.ItemStaffBinding

class StaffAdapter(private val onStaffClicked: (StaffModel) -> Unit) :
    ListAdapter<StaffModel, StaffAdapter.StaffViewHolder>(StaffDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val binding = ItemStaffBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StaffViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StaffViewHolder(private val binding: ItemStaffBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onStaffClicked(getItem(position))
                }
            }
        }

        fun bind(staff: StaffModel) {
            binding.tvName.text = staff.name
            binding.tvPosition.text = "${staff.title} - ${staff.position}"

            if (!staff.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(staff.avatarUrl)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(binding.ivAvatar)
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    class StaffDiffCallback : DiffUtil.ItemCallback<StaffModel>() {
        override fun areItemsTheSame(oldItem: StaffModel, newItem: StaffModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: StaffModel, newItem: StaffModel): Boolean {
            return oldItem == newItem
        }
    }
}