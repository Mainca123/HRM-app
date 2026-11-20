package com.example.baseproject.data.database.table

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_test")
data class TableTest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String? = null
)
