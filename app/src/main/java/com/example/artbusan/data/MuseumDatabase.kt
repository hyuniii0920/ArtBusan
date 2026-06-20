package com.example.artbusan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Museum::class], version = 1, exportSchema = false)
abstract class MuseumDatabase : RoomDatabase() {
    abstract fun museumDao(): MuseumDao

    companion object {
        @Volatile private var INSTANCE: MuseumDatabase? = null

        fun getInstance(context: Context): MuseumDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MuseumDatabase::class.java,
                    "museum.db"
                ).build().also { INSTANCE = it }
            }
    }
}
