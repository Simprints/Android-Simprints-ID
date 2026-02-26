package com.simprints.infra.protection

import com.simprints.biometrics.polyprotect.PolyProtect
import com.simprints.infra.protection.database.AuxDataDao
import com.simprints.infra.protection.database.AuxDataDatabaseFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class TemplateProtectionModule {
    @Provides
    @Singleton
    fun provideAuxDataDao(databaseFactory: AuxDataDatabaseFactory): AuxDataDao = databaseFactory
        .get()
        .auxDataDao

    // TODO PoC - configuration should come from the project
    @Provides
    fun polyProtect(): PolyProtect = PolyProtect(
        polynomialDegree = 7,
        coefficientAbsMax = 100,
        overlap = 2,
    )
}
