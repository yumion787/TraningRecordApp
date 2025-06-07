package com.example.traningrecordapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.traningrecordapp.data.dao.TrainingRecordDao
import com.example.traningrecordapp.data.entity.TrainingRecordEntity

@Database(entities = [TrainingRecordEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class TrainingDatabase : RoomDatabase() {
    abstract fun trainingRecordDao(): TrainingRecordDao

    companion object {
        @Volatile
        private var INSTANCE: TrainingDatabase? = null

        fun getDatabase(context: Context): TrainingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrainingDatabase::class.java,
                    "training_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 