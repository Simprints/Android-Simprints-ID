package com.simprints.id.testtools.di

import com.simprints.id.integration.sync.DashboardActivityAndroidTest
import com.simprints.id.activities.LaunchActivityAndroidTest
import com.simprints.id.integration.ui.CollectFingerprintsActivityTest
import com.simprints.id.integration.core.HappyWorkflowAllMainFeatures
import com.simprints.id.data.analytics.eventData.RealmSessionEventsDbManagerImplTest
import com.simprints.id.integration.sessions.SessionEventsManagerImplTest
import com.simprints.id.data.secure.SecureDataManagerTest
import com.simprints.id.di.AppComponent
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import com.simprints.id.integration.secure.AuthTestsHappyWifi
import com.simprints.id.integration.secure.AuthTestsNoWifi
import com.simprints.id.services.GuidSelectionServiceTest
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImplITest
import com.simprints.id.integration.sessions.SessionEventsUploaderTaskEndToEndTest
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (PreferencesModule::class), (SerializerModule::class)])
interface AppComponentForAndroidTests : AppComponent {
    fun inject(happyWorkflowAllMainFeatures: HappyWorkflowAllMainFeatures)
    fun inject(secureDataManagerTest: SecureDataManagerTest)
    fun inject(authTestsHappyWifi: AuthTestsHappyWifi)
    fun inject(authTestsNoWifi: AuthTestsNoWifi)
    fun inject(sessionEventsManagerImplTest: SessionEventsManagerImplTest)
    fun inject(collectFingerprintsActivityTest: CollectFingerprintsActivityTest)
    fun inject(guidSelectionServiceTest: GuidSelectionServiceTest)
    fun inject(launchActivityAndroidTest: LaunchActivityAndroidTest)
    fun inject(localSessionEventsManagerImplTest: RealmSessionEventsDbManagerImplTest)
    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
    fun inject(sessionEventsUploaderTaskEndToEndTest: SessionEventsUploaderTaskEndToEndTest)
    fun inject(sessionEentsSyncManagerImplITest: SessionEventsSyncManagerImplITest)
}
