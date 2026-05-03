package com.example.artbusan.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MuseumRepository(private val dao: MuseumDao, private val context: Context) {

    suspend fun getAll(): List<Museum> = withContext(Dispatchers.IO) {
        seedIfEmpty()
        dao.getAll()
    }

    suspend fun getByCategory(category: String): List<Museum> = withContext(Dispatchers.IO) {
        seedIfEmpty()
        dao.getByCategory(category)
    }

    suspend fun getById(id: Int): Museum? = withContext(Dispatchers.IO) {
        seedIfEmpty()
        dao.getById(id)
    }

    private suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            val lang = context.getSharedPreferences("artbusan_prefs", Context.MODE_PRIVATE)
                .getString("selected_language", "ko") ?: "ko"
            val fileName = if (lang == "ko") "museums.json" else "museums_$lang.json"
            val json = context.assets.open(fileName).bufferedReader().readText()
            val type = object : TypeToken<List<Museum>>() {}.type
            val museums: List<Museum> = Gson().fromJson(json, type)
            dao.insertAll(museums)
        }
    }
}
