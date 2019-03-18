package com.simprints.fingerprint.di;

import android.content.Context
import com.simprints.fingerprint.activities.launch.ConsentDataManager
import com.simprints.fingerprint.activities.launch.ConsentDataManagerImpl
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.tools.utils.LocationProvider
import com.simprints.fingerprint.tools.utils.LocationProviderImpl
import com.simprints.fingerprint.tools.utils.TimeHelper
import com.simprints.fingerprint.tools.utils.TimeHelperImpl
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FingerprintModule {

    @Provides
    @Singleton
    fun provideLocationProvider(ctx: Context): LocationProvider = LocationProviderImpl(ctx)

    @Provides
    @Singleton
    fun provideTimeHelper(): TimeHelper = TimeHelperImpl()


    @Provides
    @Singleton
    fun provideConsentDataManager(prefs: ImprovedSharedPreferences,
                                  remoteConfigWrapper: RemoteConfigWrapper): ConsentDataManager = ConsentDataManagerImpl(prefs, remoteConfigWrapper)

    @Provides
    @Singleton
    fun provideScannerManager(
        preferencesManager: PreferencesManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        ScannerManagerImpl(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter)

}
