package com.simprints.fingerprint.di

import android.content.Context
import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.activities.launch.ConsentDataManager
import com.simprints.fingerprint.controllers.core.consentdata.FingerprintConsentDataManager
import com.simprints.fingerprint.controllers.core.consentdata.FingerprintConsentDataManagerImpl
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManagerImpl
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManagerImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtilsImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprint.controllers.scanner.ScannerManagerImpl
import com.simprints.fingerprint.tools.utils.LocationProvider
import com.simprints.fingerprint.tools.utils.LocationProviderImpl
import com.simprints.fingerprintscanner.bluetooth.BluetoothComponentAdapter
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import dagger.Module
import dagger.Provides

@Module
class FingerprintModule {

    @Provides
    @FeatureScope
    fun provideLocationProvider(ctx: Context): LocationProvider = LocationProviderImpl(ctx)

    @Provides
    @FeatureScope
    fun provideFingerprintTimeHelper(coreTimeHelper: TimeHelper): FingerprintTimeHelper = FingerprintTimeHelperImpl(coreTimeHelper)

    @Provides
    @FeatureScope
    fun provideFingerprintDbManager(dbManager: DbManager): FingerprintDbManager = FingerprintDbManagerImpl(dbManager)

    @Provides
    @FeatureScope
    fun provideFingerprintSessionEventsManager(sessionManager: SessionEventsManager): FingerprintSessionEventsManager =
        FingerprintSessionEventsManagerImpl(sessionManager)

    @Provides
    @FeatureScope
    fun provideFingerprintCrashReportManager(crashReportManager: CrashReportManager): FingerprintCrashReportManager =
        FingerprintCrashReportManagerImpl(crashReportManager)


    @Provides
    @FeatureScope
    fun provideFingerprintConsentDataManager(consentDataManager: ConsentDataManager): FingerprintConsentDataManager =
        FingerprintConsentDataManagerImpl(consentDataManager)

    @Provides
    @FeatureScope
    fun provideSimNetworkUtils(simNetworkUtils: SimNetworkUtils): FingerprintSimNetworkUtils =
        FingerprintSimNetworkUtilsImpl(simNetworkUtils)

    @Provides
    @FeatureScope
    fun provideScannerManager(
        preferencesManager: PreferencesManager,
        analyticsManager: AnalyticsManager,
        crashReportManager: CrashReportManager,
        bluetoothComponentAdapter: BluetoothComponentAdapter): ScannerManager =
        ScannerManagerImpl(preferencesManager, analyticsManager, crashReportManager, bluetoothComponentAdapter)


}
