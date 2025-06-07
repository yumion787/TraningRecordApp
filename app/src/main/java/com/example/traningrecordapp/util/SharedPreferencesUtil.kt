package com.example.traningrecordapp.util

import android.content.Context
import android.content.SharedPreferences
import com.example.traningrecordapp.model.TrainingRecord
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class SharedPreferencesUtil(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveTrainingRecord(record: TrainingRecord) {
        val records = getTrainingRecords().toMutableList()
        records.add(record)
        val json = gson.toJson(records)
        sharedPreferences.edit().putString(KEY_RECORDS, json).apply()
    }

    fun getTrainingRecords(): List<TrainingRecord> {
        val json = sharedPreferences.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<TrainingRecord>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun deleteTrainingRecord(recordId: String) {
        val records = getTrainingRecords().filter { it.id != recordId }
        val json = gson.toJson(records)
        sharedPreferences.edit().putString(KEY_RECORDS, json).apply()
    }

    fun updateTrainingRecord(record: TrainingRecord) {
        val records = getTrainingRecords().toMutableList()
        val index = records.indexOfFirst { it.id == record.id }
        if (index != -1) {
            records[index] = record
            val json = gson.toJson(records)
            sharedPreferences.edit().putString(KEY_RECORDS, json).apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "training_records"
        private const val KEY_RECORDS = "records"
    }
} 