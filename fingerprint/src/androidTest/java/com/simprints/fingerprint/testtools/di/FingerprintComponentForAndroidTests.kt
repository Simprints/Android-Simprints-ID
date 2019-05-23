package com.simprints.fingerprint.testtools.di

import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.activities.alert.AlertActivityTest
import com.simprints.fingerprint.activities.launch.LaunchActivityAndroidTest
import com.simprints.fingerprint.di.FingerprintComponent
import com.simprints.fingerprint.di.FingerprintCoreModule
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.fingerprint.activities.collectfingerprint.CollectFingerprintsActivityTest
import com.simprints.id.di.AppComponent
import dagger.Component

@Component(
    modules = [FingerprintCoreModule::class, FingerprintModule::class],
    dependencies = [AppComponent::class])
@FeatureScope
interface FingerprintComponentForAndroidTests: FingerprintComponent {

    @Component.Builder interface Builder {

        fun appComponent(component: AppComponent): Builder
        fun fingerprintModule(module: FingerprintModule): Builder
        fun fingerprintCoreModule(module: FingerprintCoreModule): Builder
        fun build(): FingerprintComponentForAndroidTests
    }

    fun inject(launchActivityAndroidTest: LaunchActivityAndroidTest)
    fun inject(collectFingerprintsActivityTest: CollectFingerprintsActivityTest)
    fun inject(alertActivityTest: AlertActivityTest)
}
