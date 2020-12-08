package com.simprints.id.di

import android.content.Context
import com.simprints.id.activities.dashboard.DashboardViewModelFactory
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSource
import com.simprints.id.activities.dashboard.cards.daily_activity.data.DailyActivityLocalDataSourceImpl
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayer
import com.simprints.id.activities.dashboard.cards.daily_activity.displayer.DashboardDailyActivityCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepositoryImpl
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepositoryImpl
import com.simprints.id.data.db.events_sync.down.EventDownSyncScopeRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.events.RecentEventsPreferencesManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.id.services.sync.events.master.internal.EventSyncCache
import com.simprints.id.tools.time.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import dagger.Module
import dagger.Provides

@Module
open class DashboardActivityModule {

    @Provides
    open fun provideDashboardProjectDetailsCardDisplayer(): DashboardProjectDetailsCardDisplayer =
        DashboardProjectDetailsCardDisplayerImpl()

    @Provides
    open fun provideProjectDetailsRepository(
        projectRepository: ProjectRepository,
        loginInfoManager: LoginInfoManager,
        preferencesManager: PreferencesManager
    ): DashboardProjectDetailsRepository = DashboardProjectDetailsRepository(
        projectRepository,
        loginInfoManager,
        preferencesManager
    )

    @Provides
    open fun provideDashboardSyncCardStateRepository(
        eventSyncManager: EventSyncManager,
        deviceManager: DeviceManager,
        preferencesManager: PreferencesManager,
        downSyncScopeRepository: EventDownSyncScopeRepository,
        cacheSync: EventSyncCache,
        timeHelper: TimeHelper
    ): DashboardSyncCardStateRepository = DashboardSyncCardStateRepositoryImpl(
        eventSyncManager,
        deviceManager,
        preferencesManager,
        downSyncScopeRepository,
        cacheSync,
        timeHelper
    )

    @Provides
    open fun provideDailyActivityRepository(
        localDataSource: DailyActivityLocalDataSource,
        timeHelper: TimeHelper
    ): DashboardDailyActivityRepository = DashboardDailyActivityRepositoryImpl(
        localDataSource,
        timeHelper
    )

    @Provides
    open fun provideDailyActivityLocalDataSource(
        preferencesManager: RecentEventsPreferencesManager
    ): DailyActivityLocalDataSource = DailyActivityLocalDataSourceImpl(preferencesManager)

    @Provides
    open fun provideDashboardDailyActivityCardDisplayer(
        timeHelper: TimeHelper
    ): DashboardDailyActivityCardDisplayer = DashboardDailyActivityCardDisplayerImpl(timeHelper)

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

    @Provides
    open fun provideDashboardSyncCardDisplayer(
            timeHelper: TimeHelper,
            ctx: Context
    ): DashboardSyncCardDisplayer =
        DashboardSyncCardDisplayerImpl(timeHelper)

}
