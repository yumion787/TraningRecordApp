package com.example.traningrecordapp.data.dao

import androidx.room.*
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TrainingRecordDao {
    @Query("SELECT * FROM training_records ORDER BY date DESC, exerciseName ASC")
    fun getAllRecords(): Flow<List<TrainingRecordEntity>>

    @Query("SELECT * FROM training_records WHERE exerciseName = :exerciseName ORDER BY date DESC")
    fun getRecordsByExercise(exerciseName: String): Flow<List<TrainingRecordEntity>>

    @Query("SELECT * FROM training_records WHERE date = :date ORDER BY exerciseName ASC")
    fun getRecordsByDate(date: Date): Flow<List<TrainingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: TrainingRecordEntity)

    @Update
    suspend fun updateRecord(record: TrainingRecordEntity)

    @Delete
    suspend fun deleteRecord(record: TrainingRecordEntity)

    @Query("SELECT DISTINCT exerciseName FROM training_records ORDER BY exerciseName ASC")
    fun getAvailableExercises(): Flow<List<String>>
} 