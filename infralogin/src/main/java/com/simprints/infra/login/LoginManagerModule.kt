package com.simprints.infra.login

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.login.domain.AttestationManager
import com.simprints.infra.login.domain.AttestationManagerImpl
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.LoginInfoManagerImpl
import com.simprints.infra.login.extensions.deviceId
import com.simprints.infra.login.extensions.packageVersionName
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.login.network.SimApiClientFactoryImpl
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource
import com.simprints.infra.login.remote.AuthenticationRemoteDataSourceImpl
import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.infra.network.url.BaseUrlProviderImpl
import dagger.Module
import dagger.Provides

@Module
class LoginManagerModule {

    @Provides
    fun provideLoginManager(
        authenticationRemoteDataSource: AuthenticationRemoteDataSource,
        attestationManager: AttestationManager,
        loginInfoManager: LoginInfoManager,
    ): LoginManager =
        LoginManagerImpl(authenticationRemoteDataSource, attestationManager, loginInfoManager)

    @Provides
    fun provideAuthenticationRemoteDataSource(simApiClientFactory: SimApiClientFactory): AuthenticationRemoteDataSource =
        AuthenticationRemoteDataSourceImpl(simApiClientFactory)

    @Provides
    fun provideSimApiClientFactory(
        ctx: Context,
        remoteDbManager: RemoteDbManager,
        baseUrlProvider: BaseUrlProvider
    ): SimApiClientFactory = SimApiClientFactoryImpl(
        baseUrlProvider,
        ctx.deviceId,
        ctx,
        ctx.packageVersionName,
        remoteDbManager,
    )

    @Provides
    fun provideBaseUrlProvider(ctx: Context): BaseUrlProvider =
        BaseUrlProviderImpl(ctx)

    @Provides
    fun provideAttestationManager(safetyNetClient: SafetyNetClient): AttestationManager =
        AttestationManagerImpl(safetyNetClient)

    @Provides
    fun provideSafetyNetClient(context: Context): SafetyNetClient = SafetyNet.getClient(context)

    @Provides
    fun provideLoginInfoManager(context: Context): LoginInfoManager =
        LoginInfoManagerImpl(context)
}
