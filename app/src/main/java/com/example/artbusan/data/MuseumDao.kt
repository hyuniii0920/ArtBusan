package com.example.artbusan.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MuseumDao {
    @Query("SELECT * FROM museums ORDER BY id ASC")
    suspend fun getAll(): List<Museum>

    @Query("SELECT * FROM museums WHERE category = :category ORDER BY id ASC")
    suspend fun getByCategory(category: String): List<Museum>

    @Query("SELECT * FROM museums WHERE id = :id")
    suspend fun getById(id: Int): Museum?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(museums: List<Museum>)

    @Query("SELECT COUNT(*) FROM museums")
    suspend fun count(): Int
}
