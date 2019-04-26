package com.simprints.fingerprint.commontesttools.di

import android.content.Context
import com.simprints.fingerprint.controllers.consentdata.ConsentDataManager
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.locationprovider.LocationProvider
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestFingerprintModule( private var scannerManagerRule: DependencyRule = RealRule,
                             private var consentDataManagerRule: DependencyRule = RealRule,
                             private var locationProviderRule: DependencyRule = RealRule,
                             private var bluetoothComponentAdapter: DependencyRule = RealRule): FingerprintModule() {

    override fun provideScannerManager(
        preferencesManager: FingerprintPreferencesManager,
        analyticsManager: FingerprintAnalyticsManager,
        crashReportManager: FingerprintCrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        scannerManagerRule.resolveDependency { super.provideScannerManager(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter) }

    override fun provideConsentDataManager(prefs: ImprovedSharedPreferences,
                                  remoteConfigWrapper: RemoteConfigWrapper): ConsentDataManager =
        consentDataManagerRule.resolveDependency { super.provideConsentDataManager(prefs, remoteConfigWrapper) }

    override fun provideLocationProvider(ctx: Context): LocationProvider =
        locationProviderRule.resolveDependency { super.provideLocationProvider(ctx) }

    override fun provideBluetoothComponentAdapter(): BluetoothComponentAdapter =
        bluetoothComponentAdapter.resolveDependency { super.provideBluetoothComponentAdapter() }
}
