package com.simprints.id.testtools.di

import com.simprints.core.tools.coroutines.DispatcherProvider
import com.simprints.id.activities.dashboard.DashboardViewModelFactory
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.activities.login.viewmodel.LoginViewModelFactory
import com.simprints.id.activities.longConsent.PrivacyNoticeViewModelFactory
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.di.ViewModelModule
import com.simprints.id.secure.AuthenticationHelper
import com.simprints.infra.config.ConfigManager
import com.simprints.testtools.common.di.DependencyRule

class TestViewModelModule(
    private val dashboardViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val loginViewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val privacyViewModelFactoryRule: DependencyRule = DependencyRule.RealRule
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
        configManager: ConfigManager
    ): PrivacyNoticeViewModelFactory {
        return privacyViewModelFactoryRule.resolveDependency {
            super.providePrivacyNoticeViewModelFactory(
                longConsentRepository,
                configManager
            )
        }
    }
}
