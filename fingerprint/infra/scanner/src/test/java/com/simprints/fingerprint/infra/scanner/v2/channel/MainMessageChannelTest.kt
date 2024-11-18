package com.simprints.fingerprint.infra.scanner.v2.channel

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.VeroResponse
import com.simprints.fingerprint.infra.scanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprint.infra.scanner.v2.outgoing.main.MainMessageOutputStream
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.spyk
import io.reactivex.Completable
import io.reactivex.Flowable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MainMessageChannelTest {
    private lateinit var incoming: MainMessageInputStream
    private lateinit var outgoing: MainMessageOutputStream
    private lateinit var mainMessageChannel: MainMessageChannel

    var responses = mutableListOf<VeroResponse>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        incoming = spyk(MainMessageInputStream(mockk(), mockk(), mockk(), mockk())).apply {
            justRun { connect(any()) }
            veroResponses = Flowable.fromIterable(responses)
            veroEvents = Flowable.empty()
        }
        outgoing = mockk<MainMessageOutputStream> {
            justRun { connect(any()) }
            // read the next item from responses
            every { sendMessage(any()) } returns Completable.complete()
        }
        mainMessageChannel = MainMessageChannel(incoming, outgoing)
    }


    @Test(expected = RuntimeException::class)
    fun `sendMainModeCommandAndReceiveResponse should handle exceptions`() = runTest {
        // Given
        val command = mockk<OutgoingMainMessage>()
        val exception = RuntimeException("Test exception")
        coEvery { outgoing.sendMessage(command) } throws exception

        // When & Then
        mainMessageChannel.sendMainModeCommandAndReceiveResponse<VeroResponse>(command).await()
    }

    @Test
    fun `sendMainModeCommandAndReceiveResponse should send command and receive response`() =
        runTest {
            // Given
            val command = mockk<OutgoingMainMessage>()
            val response = mockk<VeroResponse>()
            responses.clear()
            responses.add(response)

            // When
            val result =
                mainMessageChannel.sendMainModeCommandAndReceiveResponse<VeroResponse>(command)
                    .await()

            // Then
            coVerify { outgoing.sendMessage(command) }
            assertThat(result).isEqualTo(response)
        }

}
