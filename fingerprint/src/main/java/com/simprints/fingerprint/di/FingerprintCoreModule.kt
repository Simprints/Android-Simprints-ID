package com.simprints.fingerprint.di

import com.simprints.core.di.FeatureScope
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManager
import com.simprints.fingerprint.controllers.core.analytics.FingerprintAnalyticsManagerImpl
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelper
import com.simprints.fingerprint.controllers.core.androidResources.FingerprintAndroidResourcesHelperImpl
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManagerImpl
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManagerImpl
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManagerImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtilsImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.AndroidResourcesHelper
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import dagger.Module
import dagger.Provides

@Module
open class FingerprintCoreModule {

    @Provides
    @FeatureScope
    open fun provideFingerprintPreferencesManager(prefs: PreferencesManager): FingerprintPreferencesManager =
        FingerprintPreferencesManagerImpl(prefs)

    @Provides
    @FeatureScope
    open fun provideAnalyicsManager(analyticsManager: AnalyticsManager): FingerprintAnalyticsManager =
        FingerprintAnalyticsManagerImpl(analyticsManager)

    @Provides
    @FeatureScope
    open fun provideFingerprintTimeHelper(coreTimeHelper: TimeHelper): FingerprintTimeHelper =
        FingerprintTimeHelperImpl(coreTimeHelper)


    @Provides
    @FeatureScope
    open fun provideFingerprintDbManager(coreDbManager: DbManager): FingerprintDbManager =
        FingerprintDbManagerImpl(coreDbManager)

    @Provides
    @FeatureScope
    open fun provideFingerprintSessionEventsManager(coreSessionManager: SessionEventsManager): FingerprintSessionEventsManager =
        FingerprintSessionEventsManagerImpl(coreSessionManager)

    @Provides
    @FeatureScope
    open fun provideFingerprintCrashReportManager(coreCrashReportManager: CoreCrashReportManager): FingerprintCrashReportManager =
        FingerprintCrashReportManagerImpl(coreCrashReportManager)

    @Provides
    @FeatureScope
    open fun provideFingerprintAndroidResourcesHelper(coreAndroidResourcesHelper: AndroidResourcesHelper): FingerprintAndroidResourcesHelper =
        FingerprintAndroidResourcesHelperImpl(coreAndroidResourcesHelper)

    @Provides
    @FeatureScope
    open fun provideSimNetworkUtils(coreSimNetworkUtils: SimNetworkUtils): FingerprintSimNetworkUtils =
        FingerprintSimNetworkUtilsImpl(coreSimNetworkUtils)
}
