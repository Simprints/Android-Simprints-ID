package com.simprints.fingerprint.capture.screen

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
import com.simprints.infra.config.sync.ConfigManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveFingerprintScanFeedbackUseCaseTest {


    private lateinit var tracker: FingerprintScanningStatusTracker

    @RelaxedMockK
    private lateinit var playAudioBeep: PlayAudioBeepUseCase

    @RelaxedMockK
    private lateinit var configManager: ConfigManager

    @RelaxedMockK
    private lateinit var scannerManager: ScannerManager

    private lateinit var notifier: ObserveFingerprintScanFeedbackUseCase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(MediaPlayer::class)
        tracker = FingerprintScanningStatusTracker()

        notifier = ObserveFingerprintScanFeedbackUseCase(
            tracker,
            playAudioBeep,
            configManager,
            scannerManager
        )
    }

    @Test
    fun `playBeep should be called when scan completes and audio is enabled`() =
        runTest(testDispatcher) {
            // Given
        //    every { sharedPreferences.getBoolean(any(), any()) } returns true

            // When
            notifier(this.backgroundScope)
            tracker.completeScan()

            // Then
            verify { playAudioBeep() }

        }

    @Test
    fun `playBeep should not be called when scan completes and audio is disabled`() =
        runTest(testDispatcher) {
            // Given
         //   every { sharedPreferences.getBoolean(any(), any()) } returns false

            // When
            notifier(this.backgroundScope)
            tracker.completeScan()

            // Then
            verify(exactly = 0) { playAudioBeep() }
        }

    @Test
    fun `releaseMediaPlayer should release the media player`() = runTest(testDispatcher) {
        //Given
//        every { sharedPreferences.getBoolean(any(), any()) } returns true

        // When
        notifier(this.backgroundScope)
        tracker.completeScan()
        notifier.stopObserving()

        // Then
        verify { playAudioBeep() }
        verify { playAudioBeep.releaseMediaPlayer() }

    }
}
