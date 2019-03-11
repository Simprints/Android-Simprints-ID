package com.simprints.fingerprint.di

import com.simprints.fingerprint.activities.collect.CollectFingerprintsPresenter
import com.simprints.fingerprint.activities.collect.scanning.CollectFingerprintsScanningHelper
import com.simprints.fingerprint.activities.launch.LaunchPresenter
import com.simprints.fingerprint.activities.matching.MatchingActivity
import com.simprints.fingerprint.activities.matching.MatchingPresenter
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules =  [FingerprintModule::class, AppModule::class, SerializerModule::class, PreferencesModule::class])
interface FingerprintsComponent {

    fun inject(matchingActivity: MatchingActivity)
    fun inject(matchingPresenter: MatchingPresenter)
    fun inject(collectFingerprintsPresenter: CollectFingerprintsPresenter)
    fun inject(collectFingerprintsScanningHelper: CollectFingerprintsScanningHelper)
    fun inject(scannerManager: ScannerManager)
    fun inject(launchPresenter: LaunchPresenter)
}
