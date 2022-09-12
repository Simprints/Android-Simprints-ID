package com.simprints.infralicense

import com.simprints.infralicense.local.LicenseLocalDataSource
import com.simprints.infralicense.local.LicenseLocalDataSourceImpl
import com.simprints.infralicense.remote.LicenseRemoteDataSource
import com.simprints.infralicense.remote.LicenseRemoteDataSourceImpl
import com.simprints.infralicense.repository.LicenseRepository
import com.simprints.infralicense.repository.LicenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class LicenseModule {

    @Binds
    abstract fun provideLicenseLocalDataSource(impl: LicenseLocalDataSourceImpl): LicenseLocalDataSource

    @Binds
    abstract fun provideLicenseRemoteDataSource(impl: LicenseRemoteDataSourceImpl): LicenseRemoteDataSource

    @Binds
    abstract fun provideLicenseRepository(impl: LicenseRepositoryImpl): LicenseRepository


}
