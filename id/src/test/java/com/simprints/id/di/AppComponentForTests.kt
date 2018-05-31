package com.simprints.id.di

import com.simprints.id.activities.AboutActivityTest
import com.simprints.id.activities.CheckLoginFromIntentActivityTest
import com.simprints.id.activities.CheckLoginFromMainLauncherActivityTest
import com.simprints.id.activities.LoginActivityTest
import com.simprints.id.activities.dashboard.DashboardCardsFactoryTest
import com.simprints.id.data.db.DbManagerTest
import com.simprints.id.secure.ProjectAuthenticatorTest
import com.simprints.id.secure.ProjectSecretManagerTest
import com.simprints.id.sync.SyncTest
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(
    AppModule::class,
    PreferencesModule::class,
    SerializerModule::class,
    AndroidInjectionModule::class))
interface AppComponentForTests : AppComponent {
    fun inject(aboutActivityTest: AboutActivityTest)
    fun inject(checkLoginFromIntentActivityTest: CheckLoginFromIntentActivityTest)
    fun inject(checkLoginFromMainLauncherActivityTest: CheckLoginFromMainLauncherActivityTest)
    fun inject(loginActivityTest: LoginActivityTest)
    fun inject(dashboardCardsFactoryTest: DashboardCardsFactoryTest)
    fun inject(syncTest: SyncTest)
    fun inject(projectSecretManagerTest: ProjectSecretManagerTest)
    fun inject(dbManagerTest: DbManagerTest)
    fun inject(projectAuthenticatorTest: ProjectAuthenticatorTest)
}
