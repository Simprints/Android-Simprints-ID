package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.alert.AlertActivityTest
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivityTest
import com.simprints.id.activities.consent.ConsentActivityTest
import com.simprints.id.activities.dashboard.DashboardCardsFactoryTest
import com.simprints.id.activities.dashboard.DashboardSyncCardViewModelTest
import com.simprints.id.activities.login.LoginActivityTest
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragmentTest
import com.simprints.id.data.consent.LongConsentManagerImplTest
import com.simprints.id.data.prefs.SettingsPreferencesManagerTest
import com.simprints.id.di.*
import com.simprints.id.secure.ProjectAuthenticatorTest
import com.simprints.id.secure.ProjectSecretManagerTest
import com.simprints.id.services.scheduledSync.imageUpSync.ImageUpSyncWorkerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.SubDownSyncTaskTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.CountWorkerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.DownSyncManagerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.DownSyncMasterWorkerTest
import com.simprints.id.services.scheduledSync.peopleDownSync.worker.SubDownSyncWorkerTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, PreferencesModule::class, SerializerModule::class, DataModule::class])
interface AppComponentForTests : AppComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder

        fun build(): AppComponentForTests
    }

    fun inject(checkLoginFromMainLauncherActivityTest: CheckLoginFromMainLauncherActivityTest)
    fun inject(loginActivityTest: LoginActivityTest)
    fun inject(dashboardCardsFactoryTest: DashboardCardsFactoryTest)
    fun inject(projectSecretManagerTest: ProjectSecretManagerTest)
    fun inject(projectAuthenticatorTest: ProjectAuthenticatorTest)
    fun inject(alertActivityTest: AlertActivityTest)
    fun inject(settingsPreferencesManagerTest: SettingsPreferencesManagerTest)
    fun inject(peopleDownSyncTaskTest: SubDownSyncTaskTest)
    fun inject(downSyncManagerTest: DownSyncManagerTest)
    fun inject(downSyncManagerWorkerTest: DownSyncMasterWorkerTest)
    fun inject(subCountWorkerTest: CountWorkerTest)
    fun inject(subDownSyncWorkerTest: SubDownSyncWorkerTest)
    fun inject(dashboardSyncCardViewModelTest: DashboardSyncCardViewModelTest)
    fun inject(settingsAboutFragmentTest: SettingsAboutFragmentTest)
    fun inject(longConsentManagerImplTest: LongConsentManagerImplTest)
    fun inject(consentActivityTest: ConsentActivityTest)
    fun inject(imageUpSyncWorkerTest: ImageUpSyncWorkerTest)
}
