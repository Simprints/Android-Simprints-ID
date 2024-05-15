package com.simprints.infra.images

import com.simprints.infra.images.local.ImageLocalDataSource
import com.simprints.infra.images.local.ImageLocalDataSourceImpl
import com.simprints.infra.images.remote.ImageRemoteDataSource
import com.simprints.infra.images.remote.ImageRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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
