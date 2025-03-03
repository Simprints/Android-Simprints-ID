package com.simprints.fingerprint.capture.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class AddBiometricReferenceCreationEventUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var eventRepo: SessionEventRepository

    private lateinit var useCase: AddBiometricReferenceCreationEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coJustRun { eventRepo.addOrUpdateEvent(any()) }

        useCase = AddBiometricReferenceCreationEventUseCase(
            timeHelper,
            eventRepo,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Adds reference creation event`() = runTest {
        useCase("id", listOf("id1", "id2", "id3"))
        coVerify {
            eventRepo.addOrUpdateEvent(
                withArg {
                    assertThat(it).isInstanceOf(BiometricReferenceCreationEvent::class.java)
                    assertThat((it.payload as BiometricReferenceCreationEvent.BiometricReferenceCreationPayload).modality)
                        .isEqualTo(BiometricReferenceCreationEvent.BiometricReferenceModality.FINGERPRINT)
                },
            )
        }
    }
}
