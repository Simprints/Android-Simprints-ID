package com.simprints.feature.logincheck.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.AuthorizationEvent
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AddAuthorizationEventUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var coreEventRepository: SessionEventRepository

    @MockK
    private lateinit var timeHelper: TimeHelper

    private lateinit var useCase: AddAuthorizationEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = AddAuthorizationEventUseCase(coreEventRepository, timeHelper)
    }

    @Test
    fun `Adds not authorised event`() = runTest {
        // When
        useCase(ActionFactory.getFlowRequest(), false)
        // Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(
                        (it as AuthorizationEvent).payload.result,
                    ).isEqualTo(AuthorizationEvent.AuthorizationPayload.AuthorizationResult.NOT_AUTHORIZED)
                    assertThat(it.payload.userInfo).isNull()
                },
            )
        }
    }

    @Test
    fun `Adds authorised event`() = runTest {
        // When
        useCase(ActionFactory.getFlowRequest(), true)
        // Then
        coVerify {
            coreEventRepository.addOrUpdateEvent(
                withArg {
                    assertThat(
                        (it as AuthorizationEvent).payload.result,
                    ).isEqualTo(AuthorizationEvent.AuthorizationPayload.AuthorizationResult.AUTHORIZED)
                    assertThat(it.payload.userInfo).isNotNull()
                },
            )
        }
    }
}
