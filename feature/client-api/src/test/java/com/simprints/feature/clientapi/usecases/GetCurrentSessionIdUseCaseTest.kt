package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.events.session.SessionEventRepository
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetCurrentSessionIdUseCaseTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: GetCurrentSessionIdUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = GetCurrentSessionIdUseCase(eventRepository)
    }

    @Test
    fun `Return current session id`() = runTest {
        coEvery { eventRepository.getCurrentSessionScope() } returns mockk {
            coEvery { id } returns "sessionId"
        }

        assertThat(useCase()).isEqualTo("sessionId")
    }
}
