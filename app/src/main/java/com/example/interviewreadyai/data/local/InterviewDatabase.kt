package com.example.interviewreadyai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InterviewSessionEntity::class, QuestionEvaluationEntity::class],
    version = 4,
    exportSchema = false
)
abstract class InterviewDatabase : RoomDatabase() {

    abstract fun interviewDao(): InterviewDao

    companion object {
        @Volatile
        private var INSTANCE: InterviewDatabase? = null

        fun getDatabase(context: Context): InterviewDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InterviewDatabase::class.java,
                    "interview_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

