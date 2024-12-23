package com.simprints.fingerprint.infra.imagedistortionconfig

import android.content.Context
import com.simprints.fingerprint.infra.imagedistortionconfig.local.ImageDistortionConfigDao
import com.simprints.fingerprint.infra.imagedistortionconfig.local.ImageDistortionConfigDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class ImageDistortionConfigModule {
    @Provides
    @Singleton
    fun provideImageDistortionConfigDatabase(
        @ApplicationContext ctx: Context,
    ): ImageDistortionConfigDatabase = ImageDistortionConfigDatabase.getDatabase(ctx)

    @Provides
    @Singleton
    fun provideImageDistortionConfigDao(database: ImageDistortionConfigDatabase): ImageDistortionConfigDao =
        database.imageDistortionConfigDao
}
