package com.simprints.id.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.person.PersonRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.session.SessionRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactory
import com.simprints.id.secure.*
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.SecurityStateProcessorImpl
import com.simprints.id.secure.securitystate.local.SecurityStatusLocalDataSource
import com.simprints.id.secure.securitystate.local.SecurityStatusLocalDataSourceImpl
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSourceImpl
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.secure.securitystate.repository.SecurityStateRepositoryImpl
import com.simprints.id.services.scheduledSync.SyncManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.securitystate.SecurityStateSchedulerImpl
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.extensions.deviceId
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
open class SecurityModule {

    @Provides
    @Singleton
    open fun provideSignerManager(
        projectRepository: ProjectRepository,
        remoteDbManager: RemoteDbManager,
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager,
        peopleSyncManager: PeopleSyncManager,
        syncManager: SyncManager,
        securityStateScheduler: SecurityStateScheduler,
        longConsentRepository: LongConsentRepository,
        sessionRepository: SessionRepository,
        baseUrlProvider: BaseUrlProvider,
        remoteConfigWrapper: RemoteConfigWrapper
    ): SignerManager = SignerManagerImpl(
        projectRepository,
        remoteDbManager,
        loginInfoManager,
        preferencesManager,
        peopleSyncManager,
        syncManager,
        securityStateScheduler,
        longConsentRepository,
        sessionRepository,
        baseUrlProvider,
        remoteConfigWrapper
    )

    @Provides
    open fun provideLoginActivityHelper(): LoginActivityHelper = LoginActivityHelperImpl()

    @Provides
    open fun provideLoginViewModelFactory(
        authenticationHelper: AuthenticationHelper
    ): LoginViewModelFactory {
        return LoginViewModelFactory(authenticationHelper)
    }

    @Provides
    open fun provideSecreteManager(loginInfoManager: LoginInfoManager): ProjectSecretManager =
        ProjectSecretManager(loginInfoManager)

    @Provides
    open fun provideAuthManager(
        apiClientFactory: SimApiClientFactory
    ): AuthManager = AuthManagerImpl(apiClientFactory)

    @Provides
    open fun provideAuthenticationDataManager(
        apiClientFactory: SimApiClientFactory,
        context: Context
    ): AuthenticationDataManager = AuthenticationDataManagerImpl(apiClientFactory, context.deviceId)

    @Provides
    open fun provideAttestationManager(): AttestationManager = AttestationManagerImpl()

    @Provides
    open fun provideProjectAuthenticator(
        authManager: AuthManager,
        projectSecretManager: ProjectSecretManager,
        loginInfoManager: LoginInfoManager,
        simApiClientFactory: SimApiClientFactory,
        baseUrlProvider: BaseUrlProvider,
        safetyNetClient: SafetyNetClient,
        secureDataManager: SecureLocalDbKeyProvider,
        projectRemoteDataSource: ProjectRemoteDataSource,
        signerManager: SignerManager,
        remoteConfigWrapper: RemoteConfigWrapper,
        longConsentRepository: LongConsentRepository,
        preferencesManager: PreferencesManager,
        attestationManager: AttestationManager,
        authenticationDataManager: AuthenticationDataManager
    ): ProjectAuthenticator = ProjectAuthenticatorImpl(
        authManager,
        projectSecretManager,
        safetyNetClient,
        secureDataManager,
        projectRemoteDataSource,
        signerManager,
        remoteConfigWrapper,
        longConsentRepository,
        preferencesManager,
        attestationManager,
        authenticationDataManager
    )

    @Provides
    open fun provideAuthenticationHelper(
        crashReportManager: CrashReportManager,
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        sessionRepository: SessionRepository
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(
            crashReportManager,
            loginInfoManager,
            timeHelper,
            projectAuthenticator,
            sessionRepository
        )
    }

    @Provides
    open fun provideSafetyNetClient(
        context: Context
    ): SafetyNetClient = SafetyNet.getClient(context)

    @Provides
    open fun provideSecurityStateRemoteDataSource(
        simApiClientFactory: SimApiClientFactory,
        loginInfoManager: LoginInfoManager,
        context: Context
    ): SecurityStateRemoteDataSource = SecurityStateRemoteDataSourceImpl(
        simApiClientFactory,
        loginInfoManager,
        context.deviceId
    )

    @Provides
    open fun provideSecurityStateRepository(
        remoteDataSource: SecurityStateRemoteDataSource,
        localDataSource: SecurityStatusLocalDataSource
    ): SecurityStateRepository = SecurityStateRepositoryImpl(
        remoteDataSource,
        localDataSource
    )

    @Provides
    open fun provideSecurityStatusLocalDataSource(
        settingsPreferencesManager: SettingsPreferencesManager
    ): SecurityStatusLocalDataSource = SecurityStatusLocalDataSourceImpl(settingsPreferencesManager)

    @Provides
    open fun provideSecurityStateScheduler(
        context: Context
    ): SecurityStateScheduler = SecurityStateSchedulerImpl(context)

    @Provides
    open fun provideSecurityStateProcessor(
        imageRepository: ImageRepository,
        personRepository: PersonRepository,
        signerManager: SignerManager
    ): SecurityStateProcessor = SecurityStateProcessorImpl(
        imageRepository,
        personRepository,
        signerManager
    )

}
