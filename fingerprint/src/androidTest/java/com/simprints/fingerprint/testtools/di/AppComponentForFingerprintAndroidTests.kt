package com.simprints.fingerprint.testtools.di

import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.di.FingerprintCoreModule
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.id.di.AppComponent
import dagger.Component

@Component(
    modules = [FingerprintCoreModule::class, FingerprintModule::class],
    dependencies = [AppComponent::class])
@FeatureScope
interface AppComponentForFingerprintAndroidTests : FingerprintComponent {

    @Component.Builder interface Builder {
        fun appComponent(component: AppComponent): Builder
        fun build(): FingerprintComponent
    }

    // Fingerprint Android tests
//    fun inject(launchActivityAndroidTest: LaunchActivityAndroidTest)
//
//    // Integration tests
//    fun inject(happyWorkflowAllMainFeatures: HappyWorkflowAllMainFeatures)
//
//    fun inject(authTestsHappyWifi: AuthTestsHappyWifi)
//    fun inject(authTestsNoWifi: AuthTestsNoWifi)
//    fun inject(sessionEventsManagerImplTest: SessionEventsManagerImplTest)
//    fun inject(collectFingerprintsActivityTest: CollectFingerprintsActivityTest)
//    fun inject(guidSelectionServiceTest: com.simprints.fingerprint.integration.services.GuidSelectionServiceTest)
//    fun inject(dashboardActivityAndroidTest: DashboardActivityAndroidTest)
//    fun inject(sessionEventsUploaderTaskEndToEndTest: SessionEventsUploaderTaskEndToEndTest)
}
