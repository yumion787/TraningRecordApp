package com.example.traningrecordapp.data.repository

import com.example.traningrecordapp.data.dao.TrainingRecordDao
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

class TrainingRepository(private val trainingRecordDao: TrainingRecordDao) {
    val allRecords: Flow<List<TrainingRecordEntity>> = trainingRecordDao.getAllRecords()
    val availableExercises: Flow<List<String>> = trainingRecordDao.getAvailableExercises()

    fun getRecordsByExercise(exerciseName: String): Flow<List<TrainingRecordEntity>> {
        return trainingRecordDao.getRecordsByExercise(exerciseName)
    }

    fun getRecordsByDate(date: Date): Flow<List<TrainingRecordEntity>> {
        return trainingRecordDao.getRecordsByDate(date)
    }

    suspend fun insertRecord(record: TrainingRecordEntity) {
        trainingRecordDao.insertRecord(record)
    }

    suspend fun updateRecord(record: TrainingRecordEntity) {
        trainingRecordDao.updateRecord(record)
    }

    suspend fun deleteRecord(record: TrainingRecordEntity) {
        trainingRecordDao.deleteRecord(record)
    }
} 