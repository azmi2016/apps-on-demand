package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BookingEntity::class, ProviderEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TaskPulseDatabase : RoomDatabase() {
    abstract fun dao(): TaskPulseDao

    companion object {
        @Volatile
        private var INSTANCE: TaskPulseDatabase? = null

        fun getDatabase(context: Context): TaskPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskPulseDatabase::class.java,
                    "taskpulse_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
