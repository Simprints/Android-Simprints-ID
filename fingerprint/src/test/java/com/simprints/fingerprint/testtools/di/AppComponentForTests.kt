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
interface FingerprintComponentForTests: FingerprintComponent {

    @Component.Builder interface Builder {

        fun appComponent(component: AppComponent): Builder
        fun fingerprintModule(module: FingerprintModule): Builder
        fun fingerprintCoreModule(module: FingerprintCoreModule): Builder
        fun build(): FingerprintComponentForTests
    }
}

