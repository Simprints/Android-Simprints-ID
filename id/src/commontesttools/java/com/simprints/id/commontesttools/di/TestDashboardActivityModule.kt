package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.activities.dashboard.DashboardViewModelFactory
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayer
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.di.DashboardActivityModule
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import com.simprints.testtools.common.di.DependencyRule

class TestDashboardActivityModule(
    private val projectDetailsCardDisplayerRule: DependencyRule = DependencyRule.RealRule,
    private val projectDetailsRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val syncCardStateRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val dailyActivityRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val dailyActivityLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val dailyActivityCardDisplayerRule: DependencyRule = DependencyRule.RealRule,
    private val viewModelFactoryRule: DependencyRule = DependencyRule.RealRule,
    private val syncCardDisplayerRule: DependencyRule = DependencyRule.RealRule
) : DashboardActivityModule() {

    override fun provideDashboardProjectDetailsCardDisplayer(
        androidResourcesHelper: AndroidResourcesHelper
    ): DashboardProjectDetailsCardDisplayer {
        return projectDetailsCardDisplayerRule.resolveDependency {
            super.provideDashboardProjectDetailsCardDisplayer(androidResourcesHelper)
        }
    }

    override fun provideProjectDetailsRepository(
        projectRepository: ProjectRepository,
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager
    ): DashboardProjectDetailsRepository {
        return projectDetailsRepositoryRule.resolveDependency {
            super.provideProjectDetailsRepository(
                projectRepository,
                loginInfoManager,
                preferencesManager
            )
        }
    }

    override fun provideDashboardSyncCardStateRepository(
        peopleSyncManager: PeopleSyncManager,
        deviceManager: DeviceManager,
        preferencesManager: PreferencesManager,
        syncScopeRepository: PeopleDownSyncScopeRepository,
        cacheSync: PeopleSyncCache,
        timeHelper: TimeHelper
    ): DashboardSyncCardStateRepository {
        return syncCardStateRepositoryRule.resolveDependency {
            super.provideDashboardSyncCardStateRepository(
                peopleSyncManager,
                deviceManager,
                preferencesManager,
                syncScopeRepository,
                cacheSync,
                timeHelper
            )
        }
    }

    override fun provideDailyActivityRepository(
        localDataSource: DailyActivityLocalDataSource,
        timeHelper: TimeHelper
    ): DashboardDailyActivityRepository {
        return dailyActivityRepositoryRule.resolveDependency {
            super.provideDailyActivityRepository(localDataSource, timeHelper)
        }
    }

    override fun provideDailyActivityLocalDataSource(preferencesManager: RecentEventsPreferencesManager): DailyActivityLocalDataSource {
        return dailyActivityLocalDataSourceRule.resolveDependency {
            super.provideDailyActivityLocalDataSource(preferencesManager)
        }
    }

    override fun provideDashboardDailyActivityCardDisplayer(
        timeHelper: TimeHelper,
        androidResourcesHelper: AndroidResourcesHelper
    ): DashboardDailyActivityCardDisplayer {
        return dailyActivityCardDisplayerRule.resolveDependency {
            super.provideDashboardDailyActivityCardDisplayer(timeHelper, androidResourcesHelper)
        }
    }

    override fun provideDashboardViewModelFactory(
        projectDetailsRepository: DashboardProjectDetailsRepository,
        syncCardStateRepository: DashboardSyncCardStateRepository,
        dailyActivityRepository: DashboardDailyActivityRepository
    ): DashboardViewModelFactory {
        return viewModelFactoryRule.resolveDependency {
            super.provideDashboardViewModelFactory(
                projectDetailsRepository,
                syncCardStateRepository,
                dailyActivityRepository
            )
        }
    }

    override fun provideDashboardSyncCardDisplayer(
        androidResourcesHelper: AndroidResourcesHelper,
        timeHelper: TimeHelper,
        ctx: Context
    ): DashboardSyncCardDisplayer {
        return syncCardDisplayerRule.resolveDependency {
            super.provideDashboardSyncCardDisplayer(androidResourcesHelper, timeHelper, ctx)
        }
    }
}
