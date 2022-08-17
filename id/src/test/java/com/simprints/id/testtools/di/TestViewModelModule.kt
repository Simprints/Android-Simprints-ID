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
import com.simprints.id.activities.settings.syncinformation.SyncInformationViewModelFactory
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.prefs.IdPreferencesManager
import com.simprints.id.di.ViewModelModule
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.id.services.sync.events.down.EventDownSyncHelper
import com.simprints.infra.login.LoginManager
import com.simprints.testtools.common.di.DependencyRule

class TestViewModelModule(
    private val dashboardViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val loginViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val privacyViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val syncInformationViewModelFactorRule: DependencyRule = DependencyRule.RealRule
) : ViewModelModule() {

    override fun provideDashboardViewModelFactory(
        projectDetailsRepository: DashboardProjectDetailsRepository,
        syncCardStateRepository: DashboardSyncCardStateRepository,
        dailyActivityRepository: DashboardDailyActivityRepository
    ): DashboardViewModelFactory {
        return dashboardViewModelFactoryRule.resolveDependency {
            super.provideDashboardViewModelFactory(projectDetailsRepository, syncCardStateRepository, dailyActivityRepository)
        }
    }

    override fun provideLoginViewModelFactory(authenticationHelper: AuthenticationHelper, dispatcher: DispatcherProvider): LoginViewModelFactory {
        return loginViewModelFactoryRule.resolveDependency {
            super.provideLoginViewModelFactory(authenticationHelper, dispatcher)
        }
    }

    override fun providePrivacyNoticeViewModelFactory(
        longConsentRepository: LongConsentRepository,
        preferencesManager: IdPreferencesManager,
        dispatcherProvider: DispatcherProvider
    ): PrivacyNoticeViewModelFactory {
        return privacyViewModelFactoryRule.resolveDependency {
            super.providePrivacyNoticeViewModelFactory(
                longConsentRepository,
                preferencesManager,
                dispatcherProvider
            )
        }
    }

    override fun provideSyncInformationViewModelFactory(
        downySyncHelper: EventDownSyncHelper,
        eventRepository: EventRepository,
        subjectRepository: SubjectRepository,
        preferencesManager: IdPreferencesManager,
        loginManager: LoginManager,
        eventDownSyncScopeRepository: EventDownSyncScopeRepository,
        imageRepository: ImageRepository,
        dispatcher: DispatcherProvider
    ): SyncInformationViewModelFactory {
        return syncInformationViewModelFactorRule.resolveDependency {
            super.provideSyncInformationViewModelFactory(
                downySyncHelper,
                eventRepository,
                subjectRepository,
                preferencesManager,
                loginManager,
                eventDownSyncScopeRepository,
                imageRepository,
                dispatcher
            )
        }
    }
}
