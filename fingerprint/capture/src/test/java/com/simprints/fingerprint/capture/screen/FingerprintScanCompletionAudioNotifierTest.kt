package com.simprints.fingerprint.capture.screen

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.preference.PreferenceManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.capture.R
import com.simprints.fingerprint.infra.scanner.capture.FingerprintScanningStatusTracker
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
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class FingerprintScanCompletionAudioNotifierTest {

    @MockK
    private lateinit var context: Context
    private lateinit var scanningStatusTracker: FingerprintScanningStatusTracker

    @RelaxedMockK
    private lateinit var mediaPlayer: MediaPlayer

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var notifier: FingerprintScanCompletionAudioNotifier
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(PreferenceManager::class)
        mockkStatic(MediaPlayer::class)
        scanningStatusTracker = FingerprintScanningStatusTracker()
        every { PreferenceManager.getDefaultSharedPreferences(context) } returns sharedPreferences
        every { MediaPlayer.create(context, R.raw.beep) } returns mediaPlayer

        notifier = FingerprintScanCompletionAudioNotifier(context, scanningStatusTracker)
    }

    @Test
    fun `playBeep should be called when scan completes and audio is enabled`() =
        runTest(testDispatcher) {
            // Given
            every { sharedPreferences.getBoolean(any(), any()) } returns true

            // When
            val job = launch { notifier.observeScanStatus() }
            scanningStatusTracker.notifyScanCompleted()

            // Then
            verify { mediaPlayer.start() }
            job.cancel()

        }

    @Test
    fun `playBeep should not be called when scan completes and audio is disabled`() =
        runTest(testDispatcher) {
            // Given
            every { sharedPreferences.getBoolean(any(), any()) } returns false

            // When
            val job = launch { notifier.observeScanStatus() }
            scanningStatusTracker.notifyScanCompleted()

            // Then
            verify(exactly = 0) { mediaPlayer.start() }
            job.cancel()

        }

    @Test
    fun `releaseMediaPlayer should release the media player`() = runTest(testDispatcher) {
        //Given
        every { sharedPreferences.getBoolean(any(), any()) } returns true

        // When
        val job = launch { notifier.observeScanStatus() }
        scanningStatusTracker.notifyScanCompleted()
        notifier.releaseMediaPlayer()

        // Then
        verify { mediaPlayer.start() }
        verify { mediaPlayer.release() }
        job.cancel()
    }
}
