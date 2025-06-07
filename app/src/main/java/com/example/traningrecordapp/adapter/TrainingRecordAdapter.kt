package com.example.traningrecordapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import com.example.traningrecordapp.databinding.ItemTrainingRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale

class TrainingRecordAdapter(
    private val onItemLongClick: (TrainingRecordEntity) -> Boolean,
    private val onItemClick: (TrainingRecordEntity) -> Unit
) : ListAdapter<TrainingRecordEntity, TrainingRecordAdapter.ViewHolder>(TrainingRecordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrainingRecordBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTrainingRecordBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                } else {
                    false
                }
            }
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(record: TrainingRecordEntity) {
            binding.apply {
                exerciseNameText.text = record.exerciseName
                weightText.text = "${record.weight}kg"
                setsRepsText.text = "${record.reps}回×${record.sets}セット"
                dateText.text = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN).format(record.date)
            }
        }
    }

    private class TrainingRecordDiffCallback : DiffUtil.ItemCallback<TrainingRecordEntity>() {
        override fun areItemsTheSame(oldItem: TrainingRecordEntity, newItem: TrainingRecordEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TrainingRecordEntity, newItem: TrainingRecordEntity): Boolean {
            return oldItem == newItem
        }
    }
} 