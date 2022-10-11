package com.simprints.id.testtools.di

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.eventsystem.event.EventRepository
import com.simprints.eventsystem.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.activities.dashboard.DashboardViewModelFactory
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.activities.longConsent.PrivacyNoticeViewModelFactory
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutViewModelFactory
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceViewModelFactory
import com.simprints.id.activities.settings.syncinformation.SyncInformationViewModelFactory
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.di.ViewModelModule
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.secure.SignerManager
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.login.LoginManager
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.testtools.common.di.DependencyRule

class TestViewModelModule(
    private val dashboardViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val loginViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val privacyViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val syncInformationViewModelFactorRule: DependencyRule = DependencyRule.RealRule,
    private val consentViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val settingAboutModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val settingsPreferenceViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
) : ViewModelModule() {

    override fun provideDashboardViewModelFactory(
        projectDetailsRepository: DashboardProjectDetailsRepository,
        syncCardStateRepository: DashboardSyncCardStateRepository,
        dailyActivityRepository: DashboardDailyActivityRepository,
        configManager: ConfigManager,
    ): DashboardViewModelFactory {
        return dashboardViewModelFactoryRule.resolveDependency {
            super.provideDashboardViewModelFactory(
                projectDetailsRepository,
                syncCardStateRepository,
                dailyActivityRepository,
                configManager
            )
        }
    }

    override fun provideLoginViewModelFactory(
        authenticationHelper: AuthenticationHelper,
        dispatcher: DispatcherProvider
    ): LoginViewModelFactory {
        return loginViewModelFactoryRule.resolveDependency {
            super.provideLoginViewModelFactory(authenticationHelper, dispatcher)
        }
    }

    override fun providePrivacyNoticeViewModelFactory(
        longConsentRepository: LongConsentRepository,
        configManager: ConfigManager,
    ): PrivacyNoticeViewModelFactory {
        return privacyViewModelFactoryRule.resolveDependency {
            super.providePrivacyNoticeViewModelFactory(
                longConsentRepository,
                configManager
            )
        }
    }

    override fun provideSyncInformationViewModelFactory(
        downySyncHelper: EventDownSyncHelper,
        eventRepository: EventRepository,
        enrolmentRecordManager: EnrolmentRecordManager,
        loginManager: LoginManager,
        eventDownSyncScopeRepository: EventDownSyncScopeRepository,
        imageRepository: ImageRepository,
        configManager: ConfigManager,
    ): SyncInformationViewModelFactory {
        return syncInformationViewModelFactorRule.resolveDependency {
            super.provideSyncInformationViewModelFactory(
                downySyncHelper,
                eventRepository,
                enrolmentRecordManager,
                loginManager,
                eventDownSyncScopeRepository,
                imageRepository,
                configManager,
            )
        }
    }

    override fun provideConsentViewModelFactory(
        configManager: ConfigManager,
        eventRepository: EventRepository
    ) =
        consentViewModelFactoryRule.resolveDependency {
            super.provideConsentViewModelFactory(configManager, eventRepository)
        }

    override fun provideSettingsAboutViewModelFactory(
        configManager: ConfigManager,
        signerManager: SignerManager,
        recentUserActivityManager: RecentUserActivityManager
    ): SettingsAboutViewModelFactory =
        settingAboutModelFactoryRule.resolveDependency {
            super.provideSettingsAboutViewModelFactory(
                configManager,
                signerManager,
                recentUserActivityManager
            )
        }

    override fun provideSettingsPreferenceViewModelFactory(configManager: ConfigManager): SettingsPreferenceViewModelFactory =
        settingsPreferenceViewModelFactoryRule.resolveDependency {
            super.provideSettingsPreferenceViewModelFactory(configManager)
        }
}
