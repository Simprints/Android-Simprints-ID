package com.simprints.id.di

import com.simprints.id.activities.*
import com.simprints.id.activities.alert.AlertActivityTest
import com.simprints.id.activities.dashboard.DashboardCardsFactoryTest
import com.simprints.id.data.db.DbManagerTest
import com.simprints.id.data.prefs.SettingsPreferencesManagerTest
import com.simprints.id.secure.ProjectAuthenticatorTest
import com.simprints.id.secure.ProjectSecretManagerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.PeopleDownSyncTaskTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.DownSyncManagerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.DownSyncMasterWorkerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.SubCountWorkerTest
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, PreferencesModule::class, SerializerModule::class, AndroidInjectionModule::class))
interface AppComponentForTests : AppComponent {
    fun inject(aboutActivityTest: AboutActivityTest)
    fun inject(checkLoginFromIntentActivityTest: CheckLoginFromIntentActivityTest)
    fun inject(checkLoginFromMainLauncherActivityTest: CheckLoginFromMainLauncherActivityTest)
    fun inject(loginActivityTest: LoginActivityTest)
    fun inject(dashboardCardsFactoryTest: DashboardCardsFactoryTest)
    fun inject(projectSecretManagerTest: ProjectSecretManagerTest)
    fun inject(dbManagerTest: DbManagerTest)
    fun inject(projectAuthenticatorTest: ProjectAuthenticatorTest)
    fun inject(alertActivityTest: AlertActivityTest)
    fun inject(launchActivityTest: LaunchActivityTest)
    fun inject(settingsPreferencesManagerTest: SettingsPreferencesManagerTest)
    fun inject(peopleDownSyncTaskTest: PeopleDownSyncTaskTest)
    fun inject(downSyncManagerTest: DownSyncManagerTest)
    fun inject(downSyncManagerWorkerTest: DownSyncMasterWorkerTest)
    fun inject(subCountWorkerTest: SubCountWorkerTest)
}
