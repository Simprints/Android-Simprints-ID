package com.simprints.infra.images

import android.content.Context
import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.local.ImageLocalDataSourceImpl
import com.simprints.infra.images.metadata.database.ImageMetadataDao
import com.simprints.infra.images.metadata.database.ImageMetadataDatabase
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.images.remote.ImageRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ImagesModule {
    @Binds
    internal abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    internal abstract fun bindImageLocalDataSource(impl: ImageLocalDataSourceImpl): ImageLocalDataSource

    @Binds
    internal abstract fun bindImageRemoteDataSource(impl: ImageRemoteDataSourceImpl): ImageRemoteDataSource
}

@Module
@InstallIn(SingletonComponent::class)
internal class ImageModuleProviders {
    @Provides
    @Singleton
    fun provideImageMetadataDatabase(
        @ApplicationContext ctx: Context,
    ): ImageMetadataDatabase = ImageMetadataDatabase.getDatabase(ctx)

    @Provides
    @Singleton
    fun provideImageMetadataDao(database: ImageMetadataDatabase): ImageMetadataDao = database.imageMetadataDao
}
