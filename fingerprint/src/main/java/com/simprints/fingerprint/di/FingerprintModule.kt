package com.simprints.fingerprint.di;

import android.content.Context
import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.ScannerManagerImpl
import com.simprints.fingerprint.tools.utils.LocationProvider
import com.simprints.fingerprint.tools.utils.LocationProviderImpl
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.Application
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class FingerprintModule(app: Application) {

    @Provides
    @Singleton
    fun provideLocationProvider(ctx: Context): LocationProvider = LocationProviderImpl(ctx)

    @Provides
    @Singleton
    fun provideScannerManager(
        preferencesManager: PreferencesManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        ScannerManagerImpl(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter)

}
