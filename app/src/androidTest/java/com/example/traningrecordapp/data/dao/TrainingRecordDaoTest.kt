package com.example.traningrecordapp.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.traningrecordapp.data.TrainingDatabase
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.util.Date

class TrainingRecordDaoTest {
    private lateinit var db: TrainingDatabase
    private lateinit var dao: TrainingRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TrainingDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.trainingRecordDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetRecord() = runBlocking {
        val record = TrainingRecordEntity(
            exerciseName = "ベンチプレス",
            weight = 50f,
            sets = 3,
            reps = 10,
            date = Date()
        )
        dao.insertRecord(record)
        val allRecords = dao.getAllRecords().first()
        assertEquals(1, allRecords.size)
        assertEquals("ベンチプレス", allRecords[0].exerciseName)
    }

    @Test
    fun updateRecord() = runBlocking {
        val record = TrainingRecordEntity(
            exerciseName = "スクワット",
            weight = 60f,
            sets = 2,
            reps = 8,
            date = Date()
        )
        dao.insertRecord(record)
        val updated = record.copy(weight = 70f)
        dao.updateRecord(updated)
        val allRecords = dao.getAllRecords().first()
        assertEquals(70f, allRecords[0].weight)
    }

    @Test
    fun deleteRecord() = runBlocking {
        val record = TrainingRecordEntity(
            exerciseName = "デッドリフト",
            weight = 80f,
            sets = 2,
            reps = 5,
            date = Date()
        )
        dao.insertRecord(record)
        dao.deleteRecord(record)
        val allRecords = dao.getAllRecords().first()
        assertEquals(0, allRecords.size)
    }
} 