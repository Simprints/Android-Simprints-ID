package com.simprints.id.di

import android.content.Context
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.sharedpreferences.PreferencesManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.id.activities.login.tools.LoginActivityHelper
import com.simprints.id.activities.login.tools.LoginActivityHelperImpl
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.secure.*
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
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.infra.login.network.SimApiClientFactory
import com.simprints.infra.network.url.BaseUrlProvider
import com.simprints.infra.security.keyprovider.SecureLocalDbKeyProvider
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
        eventRepository: EventRepository,
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
    open fun provideProjectAuthenticator(
        loginManager: LoginManager,
        secureDataManager: SecureLocalDbKeyProvider,
        projectRepository: ProjectRepository,
        signerManager: SignerManager,
        longConsentRepository: LongConsentRepository,
        preferencesManager: IdPreferencesManager,
    ): ProjectAuthenticator = ProjectAuthenticatorImpl(
        loginManager,
        secureDataManager,
        projectRepository,
        signerManager,
        longConsentRepository,
        preferencesManager,
    )

    @Provides
    open fun provideAuthenticationHelper(
        loginInfoManager: LoginInfoManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        eventRepository: EventRepository
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(
            loginInfoManager,
            timeHelper,
            projectAuthenticator,
            eventRepository
        )
    }

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
