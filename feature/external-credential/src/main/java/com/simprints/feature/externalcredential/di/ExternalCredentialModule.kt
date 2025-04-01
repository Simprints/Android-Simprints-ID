package com.simprints.feature.externalcredential.di

import com.simprints.infra.external.credential.store.model.ExternalCredential
import com.simprints.infra.external.credential.store.repository.ExternalCredentialRepository
import com.simprints.infra.external.credential.store.repository.QrExternalCredentialRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ExternalCredentialModule {

    @Binds
    abstract fun bindQrExternalCredentialRepository(
        impl: QrExternalCredentialRepository
    ): ExternalCredentialRepository<ExternalCredential.QrCode>
}
