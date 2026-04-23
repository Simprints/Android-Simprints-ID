package com.simprints.feature.setup.location

import com.google.common.truth.Truth.*
import com.simprints.infra.events.event.domain.models.scope.Location
import com.simprints.infra.events.sampledata.createSessionScope
import com.simprints.infra.events.session.SessionEventRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class UpdateSessionScopeLocationUseCaseTest {
    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var updateSessionScopeLocationUseCase: UpdateSessionScopeLocationUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { eventRepository.getCurrentSessionScope() } returns createSessionScope()

        updateSessionScopeLocationUseCase = UpdateSessionScopeLocationUseCase(eventRepository)
    }

    @Test
    fun `invoke adds location to current session scope`() = runTest {
        updateSessionScopeLocationUseCase.invoke(
            Location(
                latitude = 23.0,
                longitude = 54.0,
            ),
        )

        coVerify(exactly = 1) { eventRepository.getCurrentSessionScope() }
        coVerify(exactly = 1) {
            eventRepository.saveSessionScope(
                withArg { scope ->
                    scope.payload.location.let {
                        assertThat(it?.longitude).isEqualTo(54.0)
                        assertThat(it?.latitude).isEqualTo(23.0)
                    }
                },
            )
        }
    }
}
