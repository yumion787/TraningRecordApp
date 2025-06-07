package com.example.traningrecordapp.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import com.example.traningrecordapp.data.repository.TrainingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: TrainingRepository
    private lateinit var viewModel: TrainingViewModel
    private val dummyList = listOf(
        TrainingRecordEntity(
            exerciseName = "ベンチプレス",
            weight = 50f,
            sets = 3,
            reps = 10,
            date = Date()
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = mock()
        whenever(repository.allRecords).thenReturn(MutableStateFlow(dummyList))
        whenever(repository.availableExercises).thenReturn(MutableStateFlow(listOf("ベンチプレス")))
        val mockRepository = mock<TrainingRepository>()
        viewModel = TrainingViewModel(Mockito.mock(Application::class.java), mockRepository)
        // ViewModelのrepositoryをリフレクションで差し替え
        val repoField = TrainingViewModel::class.java.getDeclaredField("repository")
        repoField.isAccessible = true
        repoField.set(viewModel, repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun insertRecord_callsRepository() = runBlocking {
        // runBlocking:この中の処理が全部終わるまでテストを進めない
        viewModel.insertRecord("ベンチプレス", 60f, 2, 8, Date())
        // advanceUntilIdle():launchの中の処理が全部終わるまでテストを止めてくれる
        dispatcher.scheduler.advanceUntilIdle()
        verify(repository).insertRecord(any())
    }

    @Test
    fun updateRecord_callsRepository() = runBlocking {
        viewModel.updateRecord("id", "ベンチプレス", 60f, 2, 8, Date())
        dispatcher.scheduler.advanceUntilIdle()
        verify(repository).updateRecord(any())
    }

    @Test
    fun deleteRecord_callsRepository() = runBlocking {
        val record = dummyList[0]
        viewModel.deleteRecord(record)
        dispatcher.scheduler.advanceUntilIdle()
        verify(repository).deleteRecord(record)
    }

    @Test
    fun getRecordsByExercise_returnsFlow() = runBlocking {
        whenever(repository.getRecordsByExercise("ベンチプレス")).thenReturn(MutableStateFlow(dummyList))
        val result = viewModel.getRecordsByExercise("ベンチプレス").first()
        assertEquals("ベンチプレス", result[0].exerciseName)
    }
} 