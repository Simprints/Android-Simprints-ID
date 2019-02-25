package com.simprints.id.testtools.di

import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivityTest
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivityTest
import com.simprints.id.activities.launch.LaunchActivityTest
import com.simprints.id.activities.login.LoginActivityTest
import com.simprints.id.activities.alert.AlertActivityTest
import com.simprints.id.activities.dashboard.DashboardCardsFactoryTest
import com.simprints.id.activities.dashboard.DashboardSyncCardViewModelTest
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragmentTest
import com.simprints.id.data.db.DbManagerTest
import com.simprints.id.data.prefs.SettingsPreferencesManagerTest
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import com.simprints.id.secure.ProjectAuthenticatorTest
import com.simprints.id.secure.ProjectSecretManagerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.SubDownSyncTaskTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.DownSyncManagerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.DownSyncMasterWorkerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.SubCountWorkerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.SubDownSyncWorkerTest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, PreferencesModule::class, SerializerModule::class])
interface AppComponentForTests : AppComponent {
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
    fun inject(peopleDownSyncTaskTest: SubDownSyncTaskTest)
    fun inject(downSyncManagerTest: DownSyncManagerTest)
    fun inject(downSyncManagerWorkerTest: DownSyncMasterWorkerTest)
    fun inject(subCountWorkerTest: SubCountWorkerTest)
    fun inject(subDownSyncWorkerTest: SubDownSyncWorkerTest)
    fun inject(dashboardSyncCardViewModelTest: DashboardSyncCardViewModelTest)
    fun inject(settingsAboutFragmentTest: SettingsAboutFragmentTest)
}
