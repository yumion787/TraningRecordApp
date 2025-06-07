package com.example.traningrecordapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.traningrecordapp.data.TrainingDatabase
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import com.example.traningrecordapp.data.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date

class TrainingViewModel(
    application: Application,
    private val repository: TrainingRepository = TrainingRepository(TrainingDatabase.getDatabase(application).trainingRecordDao())
) : AndroidViewModel(application) {
    val allRecords: Flow<List<TrainingRecordEntity>>
    val availableExercises: Flow<List<String>>

    init {
        allRecords = repository.allRecords
        availableExercises = repository.availableExercises
    }

    fun getRecordsByExercise(exerciseName: String): Flow<List<TrainingRecordEntity>> {
        return repository.getRecordsByExercise(exerciseName)
    }

    fun getRecordsByDate(date: Date): Flow<List<TrainingRecordEntity>> {
        return repository.getRecordsByDate(date)
    }

    fun insertRecord(
        exerciseName: String,
        weight: Float,
        sets: Int,
        reps: Int,
        date: Date
    ) {
        viewModelScope.launch {
            // コルーチン(バックグラウンド処理):viewModelScope.launch で非同期実行
            // 例えば「データベースに保存」など、時間がかかる処理を画面を止めずに実行したいときに使う
            val record = TrainingRecordEntity(
                exerciseName = exerciseName,
                weight = weight,
                sets = sets,
                reps = reps,
                date = date
            )
            repository.insertRecord(record)
        }
    }

    fun updateRecord(
        id: String,
        exerciseName: String,
        weight: Float,
        sets: Int,
        reps: Int,
        date: Date
    ) {
        viewModelScope.launch {
            val record = TrainingRecordEntity(
                id = id,
                exerciseName = exerciseName,
                weight = weight,
                sets = sets,
                reps = reps,
                date = date
            )
            repository.updateRecord(record)
        }
    }

    fun deleteRecord(record: TrainingRecordEntity) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }
} 