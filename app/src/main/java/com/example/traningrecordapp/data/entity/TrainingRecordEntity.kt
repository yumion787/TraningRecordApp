package com.example.traningrecordapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "training_records")
data class TrainingRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val exerciseName: String,
    val weight: Float,
    val sets: Int,
    val reps: Int,
    val date: Date
) {
    // トレーニング量（重量 × 回数 × セット数）
    val totalVolume: Float
        get() = weight * reps * sets
} 