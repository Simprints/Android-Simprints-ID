package com.simprints.fingerprint.scanner.domain.versions

import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.logging.Simber

class ScannerHardwareRevisionsSerializer(
    private val jsonHelper: JsonHelper
) {
    /**
     * Build scanner revisions object from json string
     * In case of empty or malformed json we should create empty ota map
     * @param firmwareDownloadableVersionsJson string
     */
    fun build(firmwareDownloadableVersionsJson: String): ScannerHardwareRevisions {
        return if (firmwareDownloadableVersionsJson.isBlank()) {
            ScannerHardwareRevisions()
        } else {
            try {
                jsonHelper.fromJson(firmwareDownloadableVersionsJson)
            } catch (e: Throwable) {
                Simber.e(Exception("Malformed json", e))
                ScannerHardwareRevisions()
            }
        }
    }
}
