package com.simprints.feature.fetchsubject.screen.usecase

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.time.Timestamp
import com.simprints.feature.fetchsubject.screen.FetchSubjectState
import com.simprints.infra.events.event.domain.models.CandidateReadEvent
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.LocalResult
import com.simprints.infra.events.event.domain.models.CandidateReadEvent.CandidateReadPayload.RemoteResult
import com.simprints.infra.events.session.SessionEventRepository
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class SaveSubjectFetchEventUseCaseTest {
    @MockK
    private lateinit var eventRepository: SessionEventRepository

    private lateinit var useCase: SaveSubjectFetchEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = SaveSubjectFetchEventUseCase(eventRepository)
    }

    @Test
    fun `saves correctly mapped results for FoundLocal state`() = runTest {
        useCase(FetchSubjectState.FoundLocal, TIMESTAMP, TIMESTAMP, SUBJECT_ID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                coWithArg<CandidateReadEvent> {
                    assertThat(it.payload.localResult).isEqualTo(LocalResult.FOUND)
                    assertThat(it.payload.remoteResult).isNull()
                },
            )
        }
    }

    @Test
    fun `saves correctly mapped results for FoundRemote state`() = runTest {
        useCase(FetchSubjectState.FoundRemote, TIMESTAMP, TIMESTAMP, SUBJECT_ID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                coWithArg<CandidateReadEvent> {
                    assertThat(it.payload.localResult).isEqualTo(LocalResult.NOT_FOUND)
                    assertThat(it.payload.remoteResult).isEqualTo(RemoteResult.FOUND)
                },
            )
        }
    }

    @Test
    fun `saves correctly mapped results for NotFound state`() = runTest {
        useCase(FetchSubjectState.NotFound, TIMESTAMP, TIMESTAMP, SUBJECT_ID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                coWithArg<CandidateReadEvent> {
                    assertThat(it.payload.localResult).isEqualTo(LocalResult.NOT_FOUND)
                    assertThat(it.payload.remoteResult).isEqualTo(RemoteResult.NOT_FOUND)
                },
            )
        }
    }

    @Test
    fun `saves correctly mapped results for other states`() = runTest {
        useCase(FetchSubjectState.ConnectionError, TIMESTAMP, TIMESTAMP, SUBJECT_ID)

        coVerify {
            eventRepository.addOrUpdateEvent(
                coWithArg<CandidateReadEvent> {
                    assertThat(it.payload.localResult).isEqualTo(LocalResult.NOT_FOUND)
                    assertThat(it.payload.remoteResult).isNull()
                },
            )
        }
    }

    companion object {
        private val TIMESTAMP = Timestamp(1L)
        private const val SUBJECT_ID = "subjectID"
    }
}
