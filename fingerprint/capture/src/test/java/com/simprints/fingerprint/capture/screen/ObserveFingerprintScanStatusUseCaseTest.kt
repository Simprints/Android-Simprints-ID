package com.simprints.fingerprint.capture.screen

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.preference.PreferenceManager
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.VISUAL_SCAN_FEEDBACK
import com.simprints.infra.config.store.models.Vero2Configuration.LedsMode.LIVE_QUALITY_FEEDBACK

import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveFingerprintScanStatusUseCaseTest {

    private lateinit var tracker: FingerprintScanningStatusTracker

    @RelaxedMockK
    private lateinit var playAudioBeep: PlayAudioBeepUseCase

    @RelaxedMockK
    private lateinit var configManager: ConfigManager

    @RelaxedMockK
    private lateinit var scannerManager: ScannerManager

    @MockK
    private lateinit var fingerprintSdk: FingerprintConfiguration.BioSdk

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var observeFingerprintScanStatus: ObserveFingerprintScanStatusUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(MediaPlayer::class)
        mockkStatic(PreferenceManager::class)
        every { PreferenceManager.getDefaultSharedPreferences(context) } returns sharedPreferences
        tracker = FingerprintScanningStatusTracker(testDispatcher)

        observeFingerprintScanStatus = ObserveFingerprintScanStatusUseCase(
            tracker, playAudioBeep, configManager, scannerManager, context
        )
    }

    @Test
    fun `playBeep called when scan completes and audio and VISUAL_SCAN_FEEDBACK are enabled`() =
        runTest(testDispatcher) {
            // Given
            every { sharedPreferences.getBoolean(any(), any()) } returns true
            coEvery {
                configManager.getProjectConfiguration().fingerprint?.getSdkConfiguration(
                    fingerprintSdk
                )?.vero2?.ledsMode
            } returns VISUAL_SCAN_FEEDBACK

            // When
            observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)
            tracker.completeScan()

            // Then
            verify { playAudioBeep() }
        }

    @Test
    fun `playBeep should not be called when scan completes and audio is disabled`() =
        runTest(testDispatcher) {
            // Given
            every { sharedPreferences.getBoolean(any(), any()) } returns false

            // When
            observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)
            tracker.completeScan()

            // Then
            verify(exactly = 0) { playAudioBeep() }
        }

    @Test
    fun `releaseMediaPlayer should release the media player`() = runTest(testDispatcher) {
        //Given
        every { sharedPreferences.getBoolean(any(), any()) } returns true

        // When
        observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)
        tracker.completeScan()
        observeFingerprintScanStatus.stopObserving()

        // Then
        verify { playAudioBeep() }
        verify { playAudioBeep.releaseMediaPlayer() }

    }

    @Test
    fun `setUiGoodCapture should be called when image quality is good`() = runTest(testDispatcher) {
        // Given
        every { sharedPreferences.getBoolean(any(), any()) } returns true

        observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)
        // Simulate the previous state as ScanCompleted to allow UI change
        tracker.completeScan()

        // When
        tracker.setImageQualityCheckingResult(true)
        observeFingerprintScanStatus.stopObserving()

        // Then
        coVerify { scannerManager.scanner.setUiGoodCapture() }
    }

    @Test
    fun `setUiBadCapture should be called when image quality is bad`() = runTest(testDispatcher) {
        // Given
        every { sharedPreferences.getBoolean(any(), any()) } returns true

        observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)
        // Simulate the previous state as ScanCompleted to allow UI change
        tracker.completeScan()

        // When
        tracker.setImageQualityCheckingResult(false)
        observeFingerprintScanStatus.stopObserving()
        // Then
        coVerify { scannerManager.scanner.setUiBadCapture() }
    }

    @Test
    fun `turnOnFlashingWhiteSmileLeds should be called when state is Idle and VISUAL_SCAN_FEEDBACK is enabled`() =
        runTest(testDispatcher) {
            // Given
            every { sharedPreferences.getBoolean(any(), any()) } returns true
            coEvery {
                configManager.getProjectConfiguration().fingerprint?.getSdkConfiguration(
                    fingerprintSdk
                )?.vero2?.ledsMode
            } returns VISUAL_SCAN_FEEDBACK

            observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)

            // When
            tracker.resetToIdle()

            // Then
            coVerify { scannerManager.scanner.turnOnFlashingWhiteSmileLeds() }
        }

    @Test
    fun `turnOnFlashingWhiteSmileLeds should not be called when state is Idle and LIVE_QUALITY_FEEDBACK is enabled`() =
        runTest(testDispatcher) {
            // Given
            every { sharedPreferences.getBoolean(any(), any()) } returns true
            coEvery {
                configManager.getProjectConfiguration().fingerprint?.getSdkConfiguration(
                    fingerprintSdk
                )?.vero2?.ledsMode
            } returns LIVE_QUALITY_FEEDBACK

            observeFingerprintScanStatus(this.backgroundScope, fingerprintSdk)

            // When
            tracker.resetToIdle()

            // Then
            coVerify(exactly = 0) { scannerManager.scanner.turnOnFlashingWhiteSmileLeds() }
        }

}
