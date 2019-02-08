package com.simprints.id.di

import com.simprints.id.activities.DashboardActivityAndroidTest
import com.simprints.id.activities.LaunchActivityAndroidTest
import com.simprints.id.coreFeatures.CollectFingerprintsActivityTest
import com.simprints.id.coreFeatures.HappyWorkflowAllMainFeatures
import com.simprints.id.data.analytics.eventData.RealmSessionEventsDbManagerImplTest
import com.simprints.id.data.analytics.eventData.SessionEventsManagerImplTest
import com.simprints.id.data.secure.SecureDataManagerTest
import com.simprints.id.secure.AuthTestsHappyWifi
import com.simprints.id.secure.AuthTestsNoWifi
import com.simprints.id.service.GuidSelectionServiceTest
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsSyncManagerImplITest
import com.simprints.id.services.scheduledSync.sessionSync.SessionEventsUploaderTaskEndToEndTest
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
