package com.simprints.id.testtools.di

import com.simprints.id.Application
import com.simprints.id.activities.dashboard.DashboardActivityAndroidTest
import com.simprints.id.activities.login.LoginActivityAndroidTest
import com.simprints.id.data.analytics.eventdata.controllers.local.RealmSessionEventsDbManagerImplTest
import com.simprints.id.data.secure.SecureDataManagerTest
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import com.simprints.id.services.GuidSelectionServiceAndroidTest
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImplAndroidTest
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsUploaderTaskAndroidTest
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (PreferencesModule::class), (SerializerModule::class)])
interface AppComponentForAndroidTests : AppComponent {

    @Component.Builder interface Builder {

        @BindsInstance
        fun application(app: Application): Builder

        fun appModule(appModule: AppModule): Builder
        fun preferencesModule(preferencesModule: PreferencesModule): Builder
        fun serializerModule(serializerModule: SerializerModule): Builder

        fun build(): AppComponentForAndroidTests
    }

    fun inject(sessionEventsUploaderTaskAndroidTest: SessionEventsUploaderTaskAndroidTest)
    fun inject(loginActivityAndroidTest: LoginActivityAndroidTest)
    fun inject(guidSelectionServiceAndroidTest: GuidSelectionServiceAndroidTest)
    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
    fun inject(secureDataManagerTest: SecureDataManagerTest)
    fun inject(localSessionEventsManagerImplTest: RealmSessionEventsDbManagerImplTest)
    fun inject(sessionEventsSyncManagerImplTest: SessionEventsSyncManagerImplAndroidTest)
}
