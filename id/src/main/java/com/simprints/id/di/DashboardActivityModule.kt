package com.simprints.id.di

import android.content.Context
import com.simprints.id.activities.dashboard.DashboardViewModelFactory
import com.simprints.id.activities.dashboard.cards.daily_activity.repository.DashboardDailyActivityRepository
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayer
import com.simprints.id.activities.dashboard.cards.project.displayer.DashboardProjectDetailsCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.project.repository.DashboardProjectDetailsRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayer
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardDisplayerImpl
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepository
import com.simprints.id.activities.dashboard.cards.sync.DashboardSyncCardStateRepositoryImpl
import com.simprints.id.data.db.people_sync.down.PeopleDownSyncScopeRepository
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.people.master.PeopleSyncManager
import com.simprints.id.services.scheduledSync.people.master.internal.PeopleSyncCache
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.device.DeviceManager
import dagger.Module
import dagger.Provides

@Module
open class DashboardActivityModule {

    @Provides
    open fun provideDashboardProjectDetailsCardDisplayer(
        androidResourcesHelper: AndroidResourcesHelper
    ): DashboardProjectDetailsCardDisplayer = DashboardProjectDetailsCardDisplayerImpl(
        androidResourcesHelper
    )

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
        peopleSyncManager: PeopleSyncManager,
        deviceManager: DeviceManager,
        preferencesManager: PreferencesManager,
        syncScopeRepository: PeopleDownSyncScopeRepository,
        cacheSync: PeopleSyncCache,
        timeHelper: TimeHelper
    ): DashboardSyncCardStateRepository = DashboardSyncCardStateRepositoryImpl(
        peopleSyncManager,
        deviceManager,
        preferencesManager,
        syncScopeRepository,
        cacheSync,
        timeHelper
    )

    @Provides
    open fun provideDailyActivityRepository(
        preferencesManager: PreferencesManager
    ): DashboardDailyActivityRepository = DashboardDailyActivityRepository(preferencesManager)

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
        androidResourcesHelper: AndroidResourcesHelper,
        timeHelper: TimeHelper,
        ctx: Context
    ): DashboardSyncCardDisplayer =
        DashboardSyncCardDisplayerImpl(androidResourcesHelper, timeHelper, ctx)

}
