package com.simprints.infraimages

import com.simprints.infraimages.local.ImageLocalDataSource
import com.simprints.infraimages.local.ImageLocalDataSourceImpl
import com.simprints.infraimages.remote.ImageRemoteDataSource
import com.simprints.infraimages.remote.ImageRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class ImagesModule {

    @Binds
    internal abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    internal abstract fun bindImageLocalDataSource(impl: ImageLocalDataSourceImpl): ImageLocalDataSource

    @Binds
    internal abstract fun bindImageRemoteDataSource(impl: ImageRemoteDataSourceImpl): ImageRemoteDataSource

}
