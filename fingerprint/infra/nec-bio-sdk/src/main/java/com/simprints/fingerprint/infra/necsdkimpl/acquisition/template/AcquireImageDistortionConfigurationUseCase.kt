package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import androidx.core.content.edit
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import com.simprints.infra.security.SecurityManager
import javax.inject.Inject

@OptIn(ExperimentalStdlibApi::class)
internal class AcquireImageDistortionConfigurationUseCase @Inject constructor(
    private val fingerprintCaptureWrapperFactory: FingerprintCaptureWrapperFactory,
    securityManager: SecurityManager,
    private val recentUserActivityManager: RecentUserActivityManager
) {
    companion object {
        private const val SECURE_PREFS_FILENAME = "1bbc46c9-e911-4c5b-859f-594e5b145ec7"
        private const val DISTORTION_CONFIGURATION_KEY = "distortion_configuration"
    }

    private var sharedPreferences =
        securityManager.buildEncryptedSharedPreferences(SECURE_PREFS_FILENAME)

    // try to read the image distortion configuration from the shared preferences
    // if it's not available, acquire it from the scanner and save it in the shared preferences
    suspend operator fun invoke(): ByteArray =
        sharedPreferences.getString(getDistortionConfigurationKey(), null)?.hexToByteArray()
            ?: acquireImageDistortionConfigurationFromScanner().also {
                sharedPreferences.edit {
                    putString(getDistortionConfigurationKey(), it.toHexString())
                }
            }

    // Each scanner has a unique distortion configuration file
    // store the distortion configuration for each scanner separately using the scanner's ID as the key
    private suspend fun getDistortionConfigurationKey() =
        "$DISTORTION_CONFIGURATION_KEY-${recentUserActivityManager.getRecentUserActivity().lastScannerUsed}"

    private suspend fun acquireImageDistortionConfigurationFromScanner() =
        fingerprintCaptureWrapperFactory
            .captureWrapper
            .acquireImageDistortionMatrixConfiguration()

}

