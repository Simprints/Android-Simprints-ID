package com.simprints.fingerprint.scanner.domain.versions

import com.simprints.core.tools.json.JsonHelper
import com.simprints.logging.Simber

class ScannerHardwareRevisionsSerializer(
    private val jsonHelper: JsonHelper
) {
    /**
     * Build scanner revisions object from json string
     *
     * @param firmwareDownloadableVersionsJson string
     */
    fun build(firmwareDownloadableVersionsJson: String) = try {
        jsonHelper.fromJson(firmwareDownloadableVersionsJson)
    } catch (e: Throwable) {
        Simber.e(Exception("Malformed json", e))
        ScannerHardwareRevisions()
    }
}
