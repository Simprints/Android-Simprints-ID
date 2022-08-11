package com.simprints.infra.login

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.infra.login.db.FirebaseManagerImpl
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
import com.simprints.infra.network.SimNetwork
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class LoginManagerModule {

    @Binds
    internal abstract fun provideLoginManager(loginManager: LoginManagerImpl): LoginManager

    @Binds
    internal abstract fun provideAuthenticationRemoteDataSource(authRemoteDataSource: AuthenticationRemoteDataSourceImpl): AuthenticationRemoteDataSource

    @Binds
    internal abstract fun provideAttestationManager(impl: AttestationManagerImpl): AttestationManager

    @Binds
    internal abstract fun provideLoginInfoManager(impl: LoginInfoManagerImpl): LoginInfoManager

    @Binds
    internal abstract fun provideRemoteDbManager(impl: FirebaseManagerImpl): RemoteDbManager

}

@Module
@InstallIn(ActivityComponent::class)
object SafetyNetModule {

    @Provides
    fun provideSafetyNetClient(context: Context): SafetyNetClient = SafetyNet.getClient(context)

    @Provides
    internal fun provideSimApiClientFactory(
        ctx: Context,
        remoteDbManager: RemoteDbManager,
        baseUrlProvider: SimNetwork
    ): SimApiClientFactory = SimApiClientFactoryImpl(
        baseUrlProvider,
        ctx.deviceId,
        ctx,
        ctx.packageVersionName,
        remoteDbManager
    )

}
