package com.simprints.id.di

import android.content.Context
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetClient
import com.simprints.core.login.LoginInfoManager
import com.simprints.core.network.SimApiClientFactory
import com.simprints.core.security.SecureLocalDbKeyProvider
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.secure.AttestationManager
import com.simprints.id.secure.AttestationManagerImpl
import com.simprints.id.secure.AuthManager
import com.simprints.id.secure.AuthManagerImpl
import com.simprints.id.secure.AuthenticationDataManager
import com.simprints.id.secure.AuthenticationDataManagerImpl
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.AuthenticationHelperImpl
import com.simprints.id.secure.ProjectAuthenticator
import com.simprints.id.secure.ProjectAuthenticatorImpl
import com.simprints.id.secure.ProjectSecretManager
import com.simprints.id.secure.SignerManager
import com.simprints.id.secure.SignerManagerImpl
import com.simprints.id.secure.securitystate.SecurityStateProcessor
import com.simprints.id.secure.securitystate.SecurityStateProcessorImpl
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSource
import com.simprints.id.secure.securitystate.local.SecurityStateLocalDataSourceImpl
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSource
import com.simprints.id.secure.securitystate.remote.SecurityStateRemoteDataSourceImpl
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository
import com.simprints.id.secure.securitystate.repository.SecurityStateRepositoryImpl
import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.securitystate.SecurityStateSchedulerImpl
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.tools.extensions.deviceId
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        eventSyncManager: EventSyncManager,
        syncManager: SyncManager,
        securityStateScheduler: SecurityStateScheduler,
        longConsentRepository: LongConsentRepository,
        eventRepository: com.simprints.eventsystem.event.EventRepository,
        baseUrlProvider: BaseUrlProvider,
        remoteConfigWrapper: RemoteConfigWrapper
    ): SignerManager = SignerManagerImpl(
        projectRepository,
        remoteDbManager,
        loginInfoManager,
        preferencesManager,
        eventSyncManager,
        syncManager,
        securityStateScheduler,
        longConsentRepository,
        baseUrlProvider,
        remoteConfigWrapper
    )

    @Provides
    open fun provideLoginActivityHelper(
        securityStateRepository: SecurityStateRepository,
        jsonHelper: JsonHelper
    ): LoginActivityHelper = LoginActivityHelperImpl(securityStateRepository, jsonHelper)

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
        preferencesManager: IdPreferencesManager,
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
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        eventRepository: com.simprints.eventsystem.event.EventRepository
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(
            loginInfoManager,
            timeHelper,
            projectAuthenticator,
            eventRepository
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
    open fun provideSecurityStateLocalDataSource(
        prefs: ImprovedSharedPreferences
    ): SecurityStateLocalDataSource = SecurityStateLocalDataSourceImpl(prefs)

    @Provides
    @Singleton
    @ExperimentalCoroutinesApi
    open fun provideSecurityStateRepository(
        remoteDataSource: SecurityStateRemoteDataSource,
        localDataSource: SecurityStateLocalDataSource
    ): SecurityStateRepository = SecurityStateRepositoryImpl(remoteDataSource, localDataSource)

    @Provides
    open fun provideSecurityStateScheduler(
        context: Context
    ): SecurityStateScheduler = SecurityStateSchedulerImpl(context)

    @Provides
    open fun provideSecurityStateProcessor(
        imageRepository: ImageRepository,
        subjectRepository: SubjectRepository,
        signerManager: SignerManager
    ): SecurityStateProcessor = SecurityStateProcessorImpl(
        imageRepository,
        subjectRepository,
        signerManager
    )

}
