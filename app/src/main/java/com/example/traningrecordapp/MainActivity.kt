package com.example.traningrecordapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.traningrecordapp.adapter.TrainingRecordAdapter
import com.example.traningrecordapp.data.TrainingDatabase
import com.example.traningrecordapp.data.entity.TrainingRecordEntity
import com.example.traningrecordapp.data.repository.TrainingRepository
import com.example.traningrecordapp.databinding.ActivityMainBinding
import com.example.traningrecordapp.databinding.DialogEditRecordBinding
import com.example.traningrecordapp.databinding.DialogFilterSearchBinding
import com.example.traningrecordapp.util.DataMigrationUtil
import com.example.traningrecordapp.viewmodel.TrainingViewModel
import com.example.traningrecordapp.viewmodel.TrainingViewModelFactory
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    // ViewModel生成:Factoryを使用
    private val viewModel: TrainingViewModel by viewModels {
        val database = TrainingDatabase.getDatabase(application)
        val repository = TrainingRepository(database.trainingRecordDao())
        TrainingViewModelFactory(application, repository)
    }
    private lateinit var adapter: TrainingRecordAdapter
    private var selectedDate: Date = Date()
    private var searchSelectedDate: Date? = null
    private var chartDateToRecords: Map<String, List<TrainingRecordEntity>> = emptyMap()
    private enum class ChartMode { DAY, WEEK, MONTH }
    private var chartMode: ChartMode = ChartMode.DAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupChart()
        setupDateInput()
        setupEventListeners()
        observeViewModel()
        setupFilterButton()
        setupChartTabLayout()

        // データ移行
        val database = TrainingDatabase.getDatabase(applicationContext)
        val dataMigrationUtil = DataMigrationUtil(applicationContext, database.trainingRecordDao())
        dataMigrationUtil.migrateData()
    }

    private fun setupRecyclerView() {
        adapter = TrainingRecordAdapter(
            onItemLongClick = { record ->
                showDeleteConfirmationDialog(record)
                true
            },
            onItemClick = { record ->
                showEditRecordDialog(record)
            }
        )
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupChart() {
        binding.weightChart.apply {
            description.isEnabled = false
            legend.isEnabled = true
            setTouchEnabled(true)
            setScaleEnabled(true)
            setPinchZoom(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                labelRotationAngle = -45f
            }

            axisRight.isEnabled = false
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
            }
        }
    }

    private fun setupDateInput() {
        updateDateInputField(selectedDate)

        binding.dateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                selectedDate = calendar.time
                updateDateInputField(selectedDate)
            }, year, month, day)
            datePicker.show()
        }
    }

    private fun updateDateInputField(date: Date) {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        binding.dateInput.setText(sdf.format(date))
    }

    private fun setupEventListeners() {
        binding.recordButton.setOnClickListener {
            val exerciseName = binding.exerciseInput.text.toString()
            val weight = binding.weightInput.text.toString()
            val sets = binding.setsInput.text.toString()
            val reps = binding.repsInput.text.toString()

            if (exerciseName.isBlank()) {
                Toast.makeText(this, "筋トレ種目を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weightFloat = try {
                weight.toFloat()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "重量を正しく入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val setsInt = try {
                sets.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "セット数を正しく入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val repsInt = try {
                reps.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "回数を正しく入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (weightFloat <= 0) {
                Toast.makeText(this, "重量は0より大きい値を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (setsInt <= 0) {
                Toast.makeText(this, "セット数は0より大きい値を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (repsInt <= 0) {
                Toast.makeText(this, "回数は0より大きい値を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.insertRecord(exerciseName, weightFloat, setsInt, repsInt, selectedDate)

            // 入力欄をクリア
            binding.exerciseInput.setText("")
            binding.weightInput.setText("")
            binding.setsInput.setText("")
            binding.repsInput.setText("")
            // メッセージ表示
            Toast.makeText(this, "記録しました", Toast.LENGTH_SHORT).show()
        }

        binding.exerciseInput.setOnItemClickListener { _, _, position, _ ->
            val exerciseName = binding.exerciseInput.text.toString()
            lifecycleScope.launch {
                viewModel.getRecordsByExercise(exerciseName).collectLatest { records ->
                    updateChart(records)
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.allRecords.collectLatest { records ->
                adapter.submitList(records)
                updateExerciseSuggestions()
            }
        }

        lifecycleScope.launch {
            viewModel.availableExercises.collectLatest { exercises ->
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, exercises)
                binding.exerciseInput.setAdapter(adapter)
            }
        }
    }

    private fun updateExerciseSuggestions() {
        lifecycleScope.launch {
            viewModel.availableExercises.collectLatest { exercises ->
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, exercises)
                binding.exerciseInput.setAdapter(adapter)
            }
        }
    }

    private fun updateChart(records: List<TrainingRecordEntity>) {
        if (records.isEmpty()) {
            binding.chartTitle?.text = "トレーニング量推移"
            binding.weightChart.clear()
            binding.weightChart.invalidate()
            chartDateToRecords = emptyMap()
            return
        }
        val exerciseName = records.first().exerciseName
        binding.chartTitle?.text = "$exerciseName 推移"

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val weekFormat = SimpleDateFormat("yyyy/'W'ww", Locale.JAPAN)
        val monthFormat = SimpleDateFormat("yyyy/MM", Locale.JAPAN)
        val grouped: Map<String, List<TrainingRecordEntity>> = when (chartMode) {
            ChartMode.DAY -> records.groupBy { dateFormat.format(it.date) }
            ChartMode.WEEK -> records.groupBy { weekFormat.format(it.date) }
            ChartMode.MONTH -> records.groupBy { monthFormat.format(it.date) }
        }
        chartDateToRecords = grouped
        val volumeByDate: Map<String, Double> = grouped
            .mapValues { entry -> entry.value.sumOf { it.totalVolume.toDouble() } }
            .toSortedMap()

        val entries = volumeByDate.entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }

        val dataSet = LineDataSet(entries, "トレーニング量").apply {
            color = getColor(android.R.color.holo_blue_dark)
            setCircleColor(getColor(android.R.color.holo_blue_dark))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        binding.weightChart.data = lineData

        val xLabels = volumeByDate.keys.toList()
        binding.weightChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)

        binding.weightChart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: com.github.mikephil.charting.highlight.Highlight?) {
                if (e == null) return
                val index = e.x.toInt()
                if (index in xLabels.indices) {
                    val dateKey = xLabels[index]
                    val recordsForDate = chartDateToRecords[dateKey] ?: return
                    showChartDetailDialog(dateKey, recordsForDate)
                }
            }
            override fun onNothingSelected() {}
        })

        binding.weightChart.invalidate()
    }

    private fun showChartDetailDialog(date: String, records: List<TrainingRecordEntity>) {
        val detail = records.joinToString("\n") {
            "・${it.weight}kg × ${it.reps}回 × ${it.sets}セット"
        }
        AlertDialog.Builder(this)
            .setTitle("$date のトレーニング内訳")
            .setMessage(detail)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(record: TrainingRecordEntity) {
        AlertDialog.Builder(this)
            .setTitle("記録の削除")
            .setMessage("この記録を削除しますか？")
            .setPositiveButton("削除") { _, _ ->
                viewModel.deleteRecord(record)
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showEditRecordDialog(record: TrainingRecordEntity) {
        val dialogBinding = DialogEditRecordBinding.inflate(LayoutInflater.from(this))
        // 初期値セット
        dialogBinding.editExerciseInput.setText(record.exerciseName)
        dialogBinding.editWeightInput.setText(record.weight.toString())
        dialogBinding.editRepsInput.setText(record.reps.toString())
        dialogBinding.editSetsInput.setText(record.sets.toString())
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        dialogBinding.editDateInput.setText(sdf.format(record.date))
        var selectedEditDate = record.date
        // 日付ピッカー
        dialogBinding.editDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedEditDate
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                selectedEditDate = calendar.time
                dialogBinding.editDateInput.setText(sdf.format(selectedEditDate))
            }, year, month, day)
            datePicker.show()
        }
        // 種目オートコンプリート
        lifecycleScope.launch {
            viewModel.availableExercises.collectLatest { exercises ->
                val exerciseAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, exercises)
                dialogBinding.editExerciseInput.setAdapter(exerciseAdapter)
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("記録の編集")
            .setView(dialogBinding.root)
            .setPositiveButton("保存", null)
            .setNegativeButton("キャンセル", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newExercise = dialogBinding.editExerciseInput.text.toString()
                val newWeight = dialogBinding.editWeightInput.text.toString()
                val newReps = dialogBinding.editRepsInput.text.toString()
                val newSets = dialogBinding.editSetsInput.text.toString()

                if (newExercise.isBlank()) {
                    Toast.makeText(this, "筋トレ種目を入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val weightFloat = try {
                    newWeight.toFloat()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "重量を正しく入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val setsInt = try {
                    newSets.toInt()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "セット数を正しく入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val repsInt = try {
                    newReps.toInt()
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "回数を正しく入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (weightFloat <= 0) {
                    Toast.makeText(this, "重量は0より大きい値を入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (setsInt <= 0) {
                    Toast.makeText(this, "セット数は0より大きい値を入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (repsInt <= 0) {
                    Toast.makeText(this, "回数は0より大きい値を入力してください", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                viewModel.updateRecord(
                    record.id,
                    newExercise,
                    weightFloat,
                    setsInt,
                    repsInt,
                    selectedEditDate
                )
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun setupFilterButton() {
        binding.filterButton?.setOnClickListener {
            showFilterDialog()
        }
        binding.resetButton?.setOnClickListener {
            lifecycleScope.launch {
                viewModel.allRecords.collectLatest { records ->
                    adapter.submitList(records)
                }
            }
        }
    }

    private fun showFilterDialog() {
        val dialogBinding = DialogFilterSearchBinding.inflate(LayoutInflater.from(this))
        var selectedFilterDate: Date? = null
        // 種目オートコンプリート
        lifecycleScope.launch {
            viewModel.availableExercises.collectLatest { exercises ->
                val exerciseAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_dropdown_item_1line, exercises)
                dialogBinding.filterExerciseInput.setAdapter(exerciseAdapter)
            }
        }
        // 日付ピッカー
        dialogBinding.filterDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedFilterDate?.let { calendar.time = it }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                calendar.set(y, m, d)
                selectedFilterDate = calendar.time
                val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
                dialogBinding.filterDateInput.setText(sdf.format(selectedFilterDate!!))
            }, year, month, day)
            datePicker.show()
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("絞込み検索")
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        dialogBinding.filterSearchButton.setOnClickListener {
            val exercise = dialogBinding.filterExerciseInput.text?.toString()?.takeIf { it.isNotBlank() }
            val date = selectedFilterDate
            if (exercise != null) {
                lifecycleScope.launch {
                    viewModel.getRecordsByExercise(exercise).collectLatest { records ->
                        val filtered = if (date != null) {
                            records.filter { isSameDay(it.date, date) }
                        } else {
                            records
                        }
                        adapter.submitList(filtered)
                    }
                }
            } else if (date != null) {
                lifecycleScope.launch {
                    viewModel.getRecordsByDate(date).collectLatest { records ->
                        adapter.submitList(records)
                    }
                }
            }
            dialog.dismiss()
        }
        dialogBinding.filterCancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupChartTabLayout() {
        val tabLayout = binding.chartTabLayout
        tabLayout?.removeAllTabs()
        tabLayout?.addTab(tabLayout.newTab().setText("日"))
        tabLayout?.addTab(tabLayout.newTab().setText("週"))
        tabLayout?.addTab(tabLayout.newTab().setText("月"))
        tabLayout?.getTabAt(0)?.select()
        tabLayout?.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                chartMode = when (tab?.position) {
                    0 -> ChartMode.DAY
                    1 -> ChartMode.WEEK
                    2 -> ChartMode.MONTH
                    else -> ChartMode.DAY
                }
                // 再描画
                val exerciseName = binding.exerciseInput.text.toString()
                if (exerciseName.isNotBlank()) {
                    lifecycleScope.launch {
                        viewModel.getRecordsByExercise(exerciseName).collectLatest { records ->
                            updateChart(records)
                        }
                    }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}