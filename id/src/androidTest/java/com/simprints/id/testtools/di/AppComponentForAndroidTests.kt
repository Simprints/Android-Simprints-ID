package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.dashboard.DashboardActivityAndroidTest
import com.simprints.id.activities.login.LoginActivityAndroidTest
import com.simprints.id.activities.qrcapture.QrCaptureActivityAndroidTest
import com.simprints.id.activities.settings.ModuleSelectionActivityAndroidTest
import com.simprints.id.data.secure.LegacyLocalDbKeyProviderImplTest
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.DashboardActivityModule
import com.simprints.id.di.DataModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SecurityModule
import com.simprints.id.di.SerializerModule
import com.simprints.id.di.SyncModule
import com.simprints.id.di.ViewModelModule
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
        ViewModelModule::class
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
    fun inject(legacyLocalDbKeyProviderImplTest: LegacyLocalDbKeyProviderImplTest)
    fun inject(moduleSelectionActivityAndroidTest: ModuleSelectionActivityAndroidTest)
    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
    fun inject(qrCaptureActivityAndroidTest: QrCaptureActivityAndroidTest)
}
