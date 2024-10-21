package com.simprints.fingerprint.capture.screen


import android.content.Context
import android.media.MediaPlayer
import io.mockk.*
import org.junit.Before
import org.junit.Test

class PlayAudioBeepUseCaseTest {

    private lateinit var playAudioBeepUseCase: PlayAudioBeepUseCase
    private val mockContext = mockk<Context>()
    private val mockMediaPlayer = mockk<MediaPlayer>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(MediaPlayer::class)
        every { MediaPlayer.create(mockContext,any<Int>()) } returns mockMediaPlayer
        playAudioBeepUseCase = PlayAudioBeepUseCase(mockContext)
    }

    @Test
    fun `invoke should create and start media player if not already created`() {
        // When
        playAudioBeepUseCase()

        // Then
        verify { MediaPlayer.create(mockContext,any<Int>()) }
        verify { mockMediaPlayer.start() }
    }

    @Test
    fun `releaseMediaPlayer should release media player and set it to null`() {
        // Given
        playAudioBeepUseCase()

        // When
        playAudioBeepUseCase.releaseMediaPlayer()

        // Then
        verify { mockMediaPlayer.release() }
    }
}
