package com.example.baseproject.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.baseproject.data.database.dao.TableTestDao
import com.example.baseproject.data.database.table.TableTest

@Database(entities = [TableTest::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tableTestDao(): TableTestDao

    companion object {
        const val DATABASE_NAME = "project_base_db"
    }
}
