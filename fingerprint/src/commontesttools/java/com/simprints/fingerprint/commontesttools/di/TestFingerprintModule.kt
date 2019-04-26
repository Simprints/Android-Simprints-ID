package com.simprints.fingerprint.commontesttools.di

import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.di.FingerprintModule
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.testtools.common.di.DependencyRule

class TestFingerprintModule(
    var scannerManagerRule: DependencyRule = DependencyRule.RealRule
) : FingerprintModule() {

    override fun provideScannerManager(
        preferencesManager: PreferencesManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter
    ): ScannerManager =
        scannerManagerRule.resolveDependency { super.provideScannerManager(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter) }
}
