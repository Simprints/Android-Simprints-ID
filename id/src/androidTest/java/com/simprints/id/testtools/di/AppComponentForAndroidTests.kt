package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.dashboard.DashboardActivityAndroidTest
import com.simprints.id.activities.login.LoginActivityAndroidTest
import com.simprints.id.activities.qrcapture.QrCaptureActivityAndroidTest
import com.simprints.id.activities.settings.ModuleSelectionActivityAndroidTest
import com.simprints.id.data.db.session.remote.SessionRemoteDataSourceImplAndroidTest
import com.simprints.id.data.secure.LegacyLocalDbKeyProviderImplTest
import com.simprints.id.di.*
import com.simprints.id.services.subjects.SubjectsSyncIntegrationTest
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImplAndroidTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        PreferencesModule::class,
        SerializerModule::class,
        LoginModule::class,
        DataModule::class,
        SyncModule::class,
        DashboardActivityModule::class
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
        fun loginModule(loginModule: LoginModule): Builder

        fun build(): AppComponentForAndroidTests
    }

    fun inject(loginActivityAndroidTest: LoginActivityAndroidTest)
    fun inject(legacyLocalDbKeyProviderImplTest: LegacyLocalDbKeyProviderImplTest)
    fun inject(sessionEventsSyncManagerImplTest: SessionEventsSyncManagerImplAndroidTest)
    fun inject(moduleSelectionActivityAndroidTest: ModuleSelectionActivityAndroidTest)
    fun inject(subjectsSyncIntegrationTest: SubjectsSyncIntegrationTest)
    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
    fun inject(sessionRemoteDataSourceImplAndroidTest: SessionRemoteDataSourceImplAndroidTest)
    fun inject(qrCaptureActivityAndroidTest: QrCaptureActivityAndroidTest)
}
