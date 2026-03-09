package com.simprints.infra.aichat.database

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        FaqEntryEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@Keep
internal abstract class ChatDatabase : RoomDatabase() {
    abstract val chatDao: ChatDao
    abstract val faqDao: FaqDao

    companion object {
        private const val DB_NAME = "ai_chat_db"

        fun build(context: Context): ChatDatabase = Room
            .databaseBuilder(
                context.applicationContext,
                ChatDatabase::class.java,
                DB_NAME,
            ).fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
