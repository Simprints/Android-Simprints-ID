package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.alert.AlertActivityTest
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentPresenterTest
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivityTest
import com.simprints.id.activities.consent.ConsentActivityTest
import com.simprints.id.activities.settings.fragments.moduleselection.ModuleViewModelTest
import com.simprints.id.activities.settings.fragments.settingsAbout.SettingsAboutFragmentTest
import com.simprints.id.activities.settings.fragments.settingsPreference.SettingsPreferenceFragmentTest
import com.simprints.id.data.prefs.SettingsPreferencesManagerTest
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.DashboardActivityModule
import com.simprints.id.di.DataModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SecurityModule
import com.simprints.id.di.SerializerModule
import com.simprints.id.di.SyncModule
import com.simprints.id.di.ViewModelModule
import com.simprints.id.secure.ProjectSecretManagerTest
import com.simprints.id.services.sync.images.ImageUpSyncWorkerTest
import com.simprints.id.services.sync.subjects.down.workers.EventDownSyncCountWorkerTest
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        PreferencesModule::class,
        SerializerModule::class,
        DataModule::class,
        SyncModule::class,
        DashboardActivityModule::class,
        SecurityModule::class,
        ViewModelModule::class
    ]
)
interface AppComponentForTests : AppComponent {
    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun dataModule(dataModule: DataModule): Builder
        fun syncModule(syncModule: SyncModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder
        fun securityModule(securityModule: SecurityModule): Builder
        fun viewModelModule(viewModelModule: ViewModelModule): Builder

        fun build(): AppComponentForTests
    }

    fun inject(checkLoginFromMainLauncherActivityTest: CheckLoginFromMainLauncherActivityTest)
    fun inject(projectSecretManagerTest: ProjectSecretManagerTest)
    fun inject(alertActivityTest: AlertActivityTest)
    fun inject(settingsPreferencesManagerTest: SettingsPreferencesManagerTest)
    fun inject(settingsAboutFragmentTest: SettingsAboutFragmentTest)
    fun inject(settingsPreferenceFragmentTest: SettingsPreferenceFragmentTest)
    fun inject(moduleViewModelTest: ModuleViewModelTest)
    fun inject(consentActivityTest: ConsentActivityTest)
    fun inject(subjectsDownSyncCountWorkerTest: EventDownSyncCountWorkerTest)
    fun inject(checkLoginFromIntentPresenterTest: CheckLoginFromIntentPresenterTest)

    @ExperimentalCoroutinesApi
    fun inject(imageUpSyncWorkerTest: ImageUpSyncWorkerTest)
}
