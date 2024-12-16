package com.simprints.infra.license

import com.simprints.infra.license.local.LicenseLocalDataSource
import com.simprints.infra.license.local.LicenseLocalDataSourceImpl
import com.simprints.infra.license.remote.LicenseRemoteDataSource
import com.simprints.infra.license.remote.LicenseRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LicenseModule {
    @Binds
    internal abstract fun bindLicenseRepository(impl: LicenseRepositoryImpl): LicenseRepository

    @Binds
    internal abstract fun bindLicenseLocalDataSource(impl: LicenseLocalDataSourceImpl): LicenseLocalDataSource

    @Binds
    internal abstract fun bindLicenseRemoteDataSource(impl: LicenseRemoteDataSourceImpl): LicenseRemoteDataSource
}
