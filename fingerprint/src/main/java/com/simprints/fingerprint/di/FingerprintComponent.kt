package com.simprints.fingerprint.di

import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.activities.alert.AlertActivity
import com.simprints.fingerprint.activities.alert.AlertPresenter
import com.simprints.fingerprint.activities.collect.CollectFingerprintsActivity
import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.SplashScreenActivity
import com.simprints.fingerprint.activities.collect.scanning.CollectFingerprintsScanningHelper
import com.simprints.fingerprint.activities.launch.LaunchActivity
import com.simprints.fingerprint.activities.launch.LaunchPresenter
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.MatchingPresenter
import com.simprints.fingerprint.activities.refusal.RefusalActivity
import com.simprints.fingerprint.activities.refusal.RefusalPresenter
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.id.di.AppComponent
import dagger.Component

@Component(modules =  [FingerprintCoreModule::class, FingerprintModule::class],
           dependencies = [AppComponent::class])
@FeatureScope
interface FingerprintComponent {

    @Component.Builder interface Builder {
        fun appComponent(component: AppComponent): Builder
        fun build(): FingerprintComponent
    }

    fun inject(matchingActivity: MatchingActivity)
    fun inject(matchingPresenter: MatchingPresenter)
    fun inject(collectFingerprintsPresenter: CollectFingerprintsPresenter)
    fun inject(collectFingerprintsScanningHelper: CollectFingerprintsScanningHelper)
    fun inject(scannerManager: ScannerManager)
    fun inject(launchPresenter: LaunchPresenter)
    fun inject(alertPresenter: AlertPresenter)
    fun inject(refusalPresenter: RefusalPresenter)
    fun inject(collectFingerprintsActivity: CollectFingerprintsActivity)
    fun inject(launchActivity: LaunchActivity)
    fun inject(alertActivity: AlertActivity)
    fun inject(refusalActivity: RefusalActivity)
    fun inject(splashScreenActivity: SplashScreenActivity)
}
