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
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.enrolmentrecords.worker.EnrolmentRecordScheduler
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
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.security.SecurityManager
import com.simprints.infra.images.ImageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

// TODO: Remove after hilt migration
@DisableInstallInCheck
@Module
open class SecurityModule {

    @Provides
    @Singleton
    open fun provideSignerManager(
        configManager: ConfigManager,
        loginManager: LoginManager,
        preferencesManager: PreferencesManager,
        eventSyncManager: EventSyncManager,
        syncManager: SyncManager,
        securityStateScheduler: SecurityStateScheduler,
        longConsentRepository: LongConsentRepository,
        eventRepository: EventRepository,
        simNetwork: SimNetwork,
        remoteConfigWrapper: RemoteConfigWrapper
    ): SignerManager = SignerManagerImpl(
        configManager,
        loginManager,
        preferencesManager,
        eventSyncManager,
        syncManager,
        securityStateScheduler,
        longConsentRepository,
        simNetwork,
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
        projectSecretManager: ProjectSecretManager,
        secureDataManager: SecurityManager,
        configManager: ConfigManager,
        signerManager: SignerManager,
        longConsentRepository: LongConsentRepository,
    ): ProjectAuthenticator = ProjectAuthenticatorImpl(
        loginManager,
        projectSecretManager,
        secureDataManager,
        configManager,
        signerManager,
        longConsentRepository,
    )

    @Provides
    open fun provideAuthenticationHelper(
        loginManager: LoginManager,
        timeHelper: TimeHelper,
        projectAuthenticator: ProjectAuthenticator,
        eventRepository: EventRepository
    ): AuthenticationHelper {
        return AuthenticationHelperImpl(
            loginManager,
            timeHelper,
            projectAuthenticator,
            eventRepository
        )
    }

    @Provides
    open fun provideSecurityStateRemoteDataSource(
        loginManager: LoginManager,
        settingsPreferencesManager: SettingsPreferencesManager,
        context: Context
    ): SecurityStateRemoteDataSource = SecurityStateRemoteDataSourceImpl(
        loginManager,
        settingsPreferencesManager,
        context.deviceId
    )

    @Provides
    open fun provideSecurityStateLocalDataSource(
        prefs: ImprovedSharedPreferences
    ): SecurityStateLocalDataSource = SecurityStateLocalDataSourceImpl(prefs)

    @Provides
    open fun provideSecretManager(loginManager: LoginManager): ProjectSecretManager =
        ProjectSecretManager(loginManager)

    @Provides
    @Singleton
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
        enrolmentRecordScheduler: EnrolmentRecordScheduler,
        signerManager: SignerManager
    ): SecurityStateProcessor = SecurityStateProcessorImpl(
        imageRepository,
        subjectRepository,
        enrolmentRecordScheduler,
        signerManager
    )

}
