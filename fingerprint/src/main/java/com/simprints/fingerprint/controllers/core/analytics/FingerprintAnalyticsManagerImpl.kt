package com.simprints.fingerprint.controllers.core.analytics

import com.simprints.id.data.db.session.AnalyticsManager

class FingerprintAnalyticsManagerImpl(val analytics: AnalyticsManager) : FingerprintAnalyticsManager {

    override fun logScannerProperties(macAddress: String, scannerId: String) =
        analytics.logScannerProperties(macAddress, scannerId)

}
