package com.simprints.infra.login

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.simprints.core.DeviceID
import com.simprints.core.PackageVersionName
import com.simprints.infra.login.db.FirebaseManagerImpl
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.domain.LoginInfoManagerImpl
import com.simprints.infra.login.domain.PlayIntegrityTokenRequester
import com.simprints.infra.login.domain.PlayIntegrityTokenRequesterImpl
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.login.network.SimApiClientFactoryImpl
import com.simprints.infra.login.remote.AuthenticationRemoteDataSource
import com.simprints.infra.login.remote.AuthenticationRemoteDataSourceImpl
import com.simprints.infra.network.SimNetwork
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LoginManagerModule {

    @Binds
    internal abstract fun provideLoginManager(loginManager: LoginManagerImpl): LoginManager

    @Binds
    internal abstract fun provideAuthenticationRemoteDataSource(authRemoteDataSource: AuthenticationRemoteDataSourceImpl): AuthenticationRemoteDataSource

    @Binds
    internal abstract fun providePlayIntegrityTokenRequester(impl: PlayIntegrityTokenRequesterImpl): PlayIntegrityTokenRequester

    @Binds
    internal abstract fun provideLoginInfoManager(impl: LoginInfoManagerImpl): LoginInfoManager

    @Binds
    internal abstract fun provideRemoteDbManager(impl: FirebaseManagerImpl): RemoteDbManager

}

@Module
@InstallIn(SingletonComponent::class)
object IntegrityModule {

    @Provides
    fun providePlayIntegrityManager(@ApplicationContext context: Context) = IntegrityManagerFactory.create(context)

    @Provides
    internal fun provideSimApiClientFactory(
        @ApplicationContext ctx: Context,
        @DeviceID deviceID: String,
        @PackageVersionName packageVersionName: String,
        remoteDbManager: RemoteDbManager,
        simNetwork: SimNetwork
    ): SimApiClientFactory = SimApiClientFactoryImpl(
        simNetwork,
        deviceID,
        ctx,
        packageVersionName,
        remoteDbManager
    )

}
