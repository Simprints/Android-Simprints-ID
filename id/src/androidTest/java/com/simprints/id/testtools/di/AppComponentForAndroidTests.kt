package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.dashboard.DashboardActivityAndroidTest
import com.simprints.id.activities.login.LoginActivityAndroidTest
import com.simprints.id.activities.qrcapture.QrCaptureActivityAndroidTest
import com.simprints.id.activities.settings.ModuleSelectionActivityAndroidTest
import com.simprints.id.di.*
import com.simprints.infra.login.LoginManagerModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        PreferencesModule::class,
        SerializerModule::class,
        SecurityModule::class,
        DataModule::class,
        SyncModule::class,
        DashboardActivityModule::class,
        ViewModelModule::class,
        LoginManagerModule::class,
    ]
)
interface AppComponentForAndroidTests : AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun dataModule(dataModule: DataModule): Builder
        fun appModule(appModule: AppModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder
        fun syncModule(syncModule: SyncModule): Builder
        fun dashboardActivityModule(dashboardActivityModule: DashboardActivityModule): Builder
        fun securityModule(securityModule: SecurityModule): Builder
        fun viewModelModule(viewModelModule: ViewModelModule): Builder

        fun build(): AppComponentForAndroidTests
    }

    fun inject(loginActivityAndroidTest: LoginActivityAndroidTest)
    fun inject(moduleSelectionActivityAndroidTest: ModuleSelectionActivityAndroidTest)
    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
    fun inject(qrCaptureActivityAndroidTest: QrCaptureActivityAndroidTest)
}
