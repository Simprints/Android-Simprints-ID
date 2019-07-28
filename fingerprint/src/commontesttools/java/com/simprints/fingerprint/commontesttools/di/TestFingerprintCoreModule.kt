package com.simprints.fingerprint.commontesttools.di

import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.simnetworkutils.FingerprintSimNetworkUtils
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.di.FingerprintCoreModule
import com.simprints.id.data.analytics.crashreport.CoreCrashReportManager
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.utils.SimNetworkUtils
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.di.DependencyRule.RealRule

class TestFingerprintCoreModule(private val fingerprintTimeHelperRule: DependencyRule = RealRule,
                                private val fingerprintDbManagerRule: DependencyRule = RealRule,
                                private val fingerprintSessionEventsManagerRule: DependencyRule = RealRule,
                                private val fingerprintCrashReportManagerRule: DependencyRule = RealRule,
                                private val fingerprintSimNetworkUtilsRule: DependencyRule = RealRule,
                                private val fingerprintPreferencesManagerRuler: DependencyRule = RealRule)
    : FingerprintCoreModule() {

    override fun provideFingerprintTimeHelper(coreTimeHelper: TimeHelper): FingerprintTimeHelper =
        fingerprintTimeHelperRule.resolveDependency { super.provideFingerprintTimeHelper(coreTimeHelper) }

    override fun provideFingerprintDbManager(coreDbManager: DbManager): FingerprintDbManager =
        fingerprintDbManagerRule.resolveDependency { super.provideFingerprintDbManager(coreDbManager) }

    override fun provideFingerprintSessionEventsManager(coreSessionManager: SessionEventsManager): FingerprintSessionEventsManager =
        fingerprintSessionEventsManagerRule.resolveDependency { super.provideFingerprintSessionEventsManager(coreSessionManager) }

    override fun provideFingerprintCrashReportManager(coreCrashReportManager: CoreCrashReportManager): FingerprintCrashReportManager =
        fingerprintCrashReportManagerRule.resolveDependency { super.provideFingerprintCrashReportManager(coreCrashReportManager) }

    override fun provideSimNetworkUtils(coreSimNetworkUtils: SimNetworkUtils): FingerprintSimNetworkUtils =
        fingerprintSimNetworkUtilsRule.resolveDependency { super.provideSimNetworkUtils(coreSimNetworkUtils) }

    override fun provideFingerprintPreferencesManager(corePreferencesManager: PreferencesManager): FingerprintPreferencesManager =
        fingerprintPreferencesManagerRuler.resolveDependency { super.provideFingerprintPreferencesManager(corePreferencesManager) }
}
