package com.simprints.fingerprint.infra.imagedistortionconfig.local

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DbImageDistortionConfig::class],
    version = 1,
    exportSchema = false,
)
@Keep
internal abstract class ImageDistortionConfigDatabase : RoomDatabase() {
    abstract val imageDistortionConfigDao: ImageDistortionConfigDao

    companion object {
        private const val ROOM_DB_NAME = "image_distortion_config_db"

        fun getDatabase(context: Context): ImageDistortionConfigDatabase = Room
            .databaseBuilder(
                context.applicationContext,
                ImageDistortionConfigDatabase::class.java,
                ROOM_DB_NAME,
            ).fallbackToDestructiveMigration()
            .build()
    }
}
