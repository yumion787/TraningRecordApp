package com.example.traningrecordapp.model

import java.util.Date
import java.util.UUID

data class TrainingRecord(
    val id: String = UUID.randomUUID().toString(),
    val exerciseName: String,
    val weight: Float,
    val sets: Int,
    val reps: Int,
    val date: Date = Date()
) {
    // トレーニング量（重量 × 回数 × セット数）
    val totalVolume: Float
        get() = weight * reps * sets

    companion object {
        const val KEY_ID = "id"
        const val KEY_EXERCISE_NAME = "exercise_name"
        const val KEY_WEIGHT = "weight"
        const val KEY_SETS = "sets"
        const val KEY_REPS = "reps"
        const val KEY_DATE = "date"
    }
} 