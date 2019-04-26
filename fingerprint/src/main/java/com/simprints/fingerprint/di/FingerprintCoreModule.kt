package com.simprints.fingerprint.di

import com.simprints.core.di.FeatureScope
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
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import dagger.Module
import dagger.Provides

@Module
class FingerprintCoreModule {

    @Provides
    @FeatureScope
    fun provideFingerprintTimeHelper(coreTimeHelper: TimeHelper): FingerprintTimeHelper =
        FingerprintTimeHelperImpl(coreTimeHelper)

    @Provides
    @FeatureScope
    fun provideFingerprintDbManager(coreDbManager: DbManager): FingerprintDbManager =
        FingerprintDbManagerImpl(coreDbManager)

    @Provides
    @FeatureScope
    fun provideFingerprintSessionEventsManager(coreSessionManager: SessionEventsManager): FingerprintSessionEventsManager =
        FingerprintSessionEventsManagerImpl(coreSessionManager)

    @Provides
    @FeatureScope
    fun provideFingerprintCrashReportManager(coreCrashReportManager: CrashReportManager): FingerprintCrashReportManager =
        FingerprintCrashReportManagerImpl(coreCrashReportManager)


    @Provides
    @FeatureScope
    fun provideSimNetworkUtils(coreSimNetworkUtils: SimNetworkUtils): FingerprintSimNetworkUtils =
        FingerprintSimNetworkUtilsImpl(coreSimNetworkUtils)
}
