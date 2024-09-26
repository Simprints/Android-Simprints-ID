package com.simprints.infra.images.metadata.database

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DbImageMetadata::class],
    version = 1,
    exportSchema = false
)
@Keep
internal abstract class ImageMetadataDatabase : RoomDatabase() {

    abstract val imageMetadataDao: ImageMetadataDao

    companion object {

        private const val ROOM_DB_NAME = "image_meta_db"

        fun getDatabase(context: Context): ImageMetadataDatabase = Room
            .databaseBuilder(
                context.applicationContext,
                ImageMetadataDatabase::class.java,
                ROOM_DB_NAME
            )
            .fallbackToDestructiveMigration()
            .build()
    }
}
