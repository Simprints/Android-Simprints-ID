package com.simprints.id.di

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.core.tools.time.TimeHelper
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.activities.consent.ConsentViewModelFactory
import com.simprints.id.activities.coreexitform.CoreExitFormViewModelFactory
import com.simprints.id.activities.dashboard.DashboardViewModelFactory
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.activities.enrollast.EnrolLastBiometricsViewModelFactory
import com.simprints.id.activities.faceexitform.FaceExitFormViewModelFactory
import com.simprints.id.activities.fetchguid.FetchGuidHelper
import com.simprints.id.activities.fetchguid.FetchGuidViewModelFactory
import com.simprints.id.activities.fingerprintexitform.FingerprintExitFormViewModelFactory
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.activities.longConsent.PrivacyNoticeViewModelFactory
import com.simprints.id.activities.orchestrator.OrchestratorEventsHelper
import com.simprints.id.activities.orchestrator.OrchestratorViewModelFactory
import com.simprints.id.activities.settings.fingerselection.FingerSelectionViewModelFactory
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleSelectionViewModelFactory
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutViewModelFactory
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceViewModelFactory
import com.simprints.id.activities.settings.syncinformation.SyncInformationViewModelFactory
import com.simprints.id.activities.setup.SetupViewModelFactory
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.domain.moduleapi.app.DomainToModuleApiAppResponse
import com.simprints.id.moduleselection.ModuleRepository
import com.simprints.id.orchestrator.EnrolmentHelper
import com.simprints.id.orchestrator.OrchestratorManager
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.SignerManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.tools.device.DeviceManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi

@Module
open class ViewModelModule {

    @Provides
    open fun provideModuleViewModelFactory(
        repository: ModuleRepository,
        eventSyncManager: EventSyncManager
    ) =
        ModuleSelectionViewModelFactory(repository, eventSyncManager)

    @Provides
    open fun provideConsentViewModelFactory(
        configManager: ConfigManager,
        eventRepository: EventRepository
    ) =
        ConsentViewModelFactory(configManager, eventRepository)

    @Provides
    open fun provideCoreExitFormViewModelFactory(eventRepository: EventRepository) =
        CoreExitFormViewModelFactory(eventRepository)

    @Provides
    open fun provideFingerprintExitFormViewModelFactory(eventRepository: EventRepository) =
        FingerprintExitFormViewModelFactory(eventRepository)

    @Provides
    open fun provideFaceExitFormViewModelFactory(eventRepository: EventRepository) =
        FaceExitFormViewModelFactory(eventRepository)

    @Provides
    open fun provideFetchGuidViewModelFactory(
        guidFetchGuidHelper: FetchGuidHelper,
        deviceManager: DeviceManager,
        eventRepository: EventRepository,
        timeHelper: TimeHelper,
        dispatcher: DispatcherProvider
    ) = FetchGuidViewModelFactory(
        guidFetchGuidHelper,
        deviceManager,
        eventRepository,
        timeHelper,
        dispatcher
    )

    @Provides
    open fun provideSyncInformationViewModelFactory(
        downySyncHelper: EventDownSyncHelper,
        eventRepository: EventRepository,
        subjectRepository: SubjectRepository,
        loginManager: LoginManager,
        eventDownSyncScopeRepository: EventDownSyncScopeRepository,
        imageRepository: ImageRepository,
        configManager: ConfigManager,
    ): SyncInformationViewModelFactory =
        SyncInformationViewModelFactory(
            downySyncHelper,
            eventRepository,
            subjectRepository,
            loginManager.getSignedInProjectIdOrEmpty(),
            eventDownSyncScopeRepository,
            imageRepository,
            configManager
        )

    @Provides
    open fun provideFingerSelectionViewModelFactory(
        configManager: ConfigManager
    ) = FingerSelectionViewModelFactory(configManager)

    @Provides
    open fun providePrivacyNoticeViewModelFactory(
        longConsentRepository: LongConsentRepository,
        configManager: ConfigManager,
    ) = PrivacyNoticeViewModelFactory(longConsentRepository, configManager)

    @Provides
    open fun provideEnrolLastBiometricsViewModel(
        enrolmentHelper: EnrolmentHelper,
        timeHelper: TimeHelper,
        configManager: ConfigManager
    ) = EnrolLastBiometricsViewModelFactory(enrolmentHelper, timeHelper, configManager)

    @ExperimentalCoroutinesApi
    @Provides
    open fun provideSetupViewModelFactory(
        deviceManager: DeviceManager
    ) = SetupViewModelFactory(deviceManager)

    @Provides
    open fun provideSettingsPreferenceViewModelFactory(configManager: ConfigManager): SettingsPreferenceViewModelFactory {
        return SettingsPreferenceViewModelFactory(configManager)
    }

    @Provides
    open fun provideSettingsAboutViewModelFactory(
        configManager: ConfigManager,
        signerManager: SignerManager
    ): SettingsAboutViewModelFactory {
        return SettingsAboutViewModelFactory(configManager, signerManager)
    }

    @Provides
    open fun provideLoginViewModelFactory(
        authenticationHelper: AuthenticationHelper,
        dispatcher: DispatcherProvider
    ): LoginViewModelFactory {
        return LoginViewModelFactory(authenticationHelper, dispatcher)
    }

    @Provides
    fun provideOrchestratorViewModelFactory(
        orchestratorManager: OrchestratorManager,
        orchestratorEventsHelper: OrchestratorEventsHelper,
        configManager: ConfigManager,
        eventRepository: EventRepository
    ): OrchestratorViewModelFactory {
        return OrchestratorViewModelFactory(
            orchestratorManager,
            orchestratorEventsHelper,
            configManager,
            eventRepository,
            DomainToModuleApiAppResponse
        )
    }

    @Provides
    open fun provideDashboardViewModelFactory(
        projectDetailsRepository: DashboardProjectDetailsRepository,
        syncCardStateRepository: DashboardSyncCardStateRepository,
        dailyActivityRepository: DashboardDailyActivityRepository
    ): DashboardViewModelFactory {
        return DashboardViewModelFactory(
            projectDetailsRepository,
            syncCardStateRepository,
            dailyActivityRepository
        )
    }
}
