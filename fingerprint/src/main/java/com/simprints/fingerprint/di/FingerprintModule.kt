package com.simprints.fingerprint.di

import android.content.Context
import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManagerImpl
import com.simprints.fingerprint.controllers.locationProvider.LocationProvider
import com.simprints.fingerprint.controllers.locationProvider.LocationProviderImpl
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.controllers.scanner.ScannerManagerImpl
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import dagger.Module
import dagger.Provides

@Module
class FingerprintModule {

    @Provides
    @FeatureScope
    fun provideScannerManager(
        preferencesManager: PreferencesManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        ScannerManagerImpl(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter)

    @Provides
    @FeatureScope
    fun provideConsentDataManager(prefs: ImprovedSharedPreferences,
                                  remoteConfigWrapper: RemoteConfigWrapper): ConsentDataManager =
        ConsentDataManagerImpl(prefs, remoteConfigWrapper)

    @Provides
    @FeatureScope
    fun provideLocationProvider(ctx: Context): LocationProvider = LocationProviderImpl(ctx)
}
