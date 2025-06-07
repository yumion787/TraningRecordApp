package com.example.traningrecordapp.util

import android.content.Context
import com.example.traningrecordapp.data.dao.TrainingRecordDao
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataMigrationUtil(
    private val context: Context,
    private val dao: TrainingRecordDao
) {
    private val sharedPreferencesUtil = SharedPreferencesUtil(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun migrateData() {
        scope.launch {
            val oldRecords = sharedPreferencesUtil.getTrainingRecords()
            oldRecords.forEach { oldRecord ->
                val newRecord = TrainingRecordEntity(
                    id = oldRecord.id,
                    exerciseName = oldRecord.exerciseName,
                    weight = oldRecord.weight,
                    sets = oldRecord.sets,
                    reps = oldRecord.reps,
                    date = oldRecord.date
                )
                dao.insertRecord(newRecord)
            }
        }
    }
} 