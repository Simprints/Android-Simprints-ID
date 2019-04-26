package com.simprints.fingerprint.testtools.di

import com.simprints.fingerprint.activities.launch.LaunchActivityAndroidTest
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.fingerprint.di.FingerprintsComponent
import com.simprints.fingerprint.integration.core.HappyWorkflowAllMainFeatures
import com.simprints.fingerprint.integration.secure.AuthTestsHappyWifi
import com.simprints.fingerprint.integration.secure.AuthTestsNoWifi
import com.simprints.fingerprint.integration.sessions.SessionEventsManagerImplTest
import com.simprints.fingerprint.integration.sessions.SessionEventsUploaderTaskEndToEndTest
import com.simprints.fingerprint.integration.sync.DashboardActivityAndroidTest
import com.simprints.fingerprint.integration.ui.CollectFingerprintsActivityTest
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(AppModule::class), (PreferencesModule::class), (SerializerModule::class), (FingerprintModule::class)])
interface AppComponentForFingerprintAndroidTests : FingerprintsComponent {

    // Fingerprint Android tests
    fun inject(launchActivityAndroidTest: LaunchActivityAndroidTest)

    // Integration tests
    fun inject(happyWorkflowAllMainFeatures: HappyWorkflowAllMainFeatures)
    fun inject(authTestsHappyWifi: AuthTestsHappyWifi)
    fun inject(authTestsNoWifi: AuthTestsNoWifi)
    fun inject(sessionEventsManagerImplTest: SessionEventsManagerImplTest)
    fun inject(collectFingerprintsActivityTest: CollectFingerprintsActivityTest)
    fun inject(guidSelectionServiceTest: com.simprints.fingerprint.integration.services.GuidSelectionServiceTest)
    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
    fun inject(sessionEventsUploaderTaskEndToEndTest: SessionEventsUploaderTaskEndToEndTest)
}
