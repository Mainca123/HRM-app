package com.example.baseproject.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.baseproject.data.database.table.TableTest
import kotlinx.coroutines.flow.Flow

@Dao
interface TableTestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tableTest: TableTest)

    @Query("SELECT * FROM table_test")
    fun getList(): Flow<List<TableTest>>

    @Delete
    suspend fun delete(tableTest: TableTest)
}
