package com.example.artbusan.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "museums")
data class Museum(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val location: String,
    val hours: String,
    val fee: String,
    val phone: String,
    val description: String,
    val imageUrl: String
)
