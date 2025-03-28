package com.tlucontact.ui.units

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tlucontact.data.models.UnitModel  // Updated import
import com.tlucontact.databinding.ItemUnitBinding

class UnitsAdapter(private val onUnitClicked: (UnitModel) -> Unit) :  // Updated parameter type
    ListAdapter<UnitModel, UnitsAdapter.UnitViewHolder>(UnitDiffCallback()) {  // Updated generic type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UnitViewHolder {
        val binding = ItemUnitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UnitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UnitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UnitViewHolder(private val binding: ItemUnitBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUnitClicked(getItem(position))
                }
            }
        }

        fun bind(unit: UnitModel) {  // Updated parameter type
            binding.tvName.text = unit.name
            binding.tvCode.text = "Mã đơn vị: ${unit.code}"
            binding.tvType.text = unit.type

            // Here you would load the logo if it was available
            // To simplify for the free tier, we'll use the default icon
            // Glide.with(binding.root.context)
            //     .load(unit.logoURL)
            //     .placeholder(R.drawable.ic_building)
            //     .into(binding.ivLogo)
        }
    }

    class UnitDiffCallback : DiffUtil.ItemCallback<UnitModel>() {  // Updated generic type
        override fun areItemsTheSame(oldItem: UnitModel, newItem: UnitModel): Boolean {  // Updated parameter types
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UnitModel, newItem: UnitModel): Boolean {  // Updated parameter types
            return oldItem == newItem
        }
    }
}