package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowProvider
import com.simprints.infra.enrolment.records.domain.models.SubjectQuery
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent
import com.simprints.matcher.FaceMatchResult
import com.simprints.matcher.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SaveMatchEventUseCaseTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var eventRepository: EventRepository

    private lateinit var useCase: SaveMatchEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs

        useCase = SaveMatchEventUseCase(
            eventRepository,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher)
        )
    }

    @Test
    fun `Correctly saves one to one match event`() = runTest {
        useCase.invoke(
            1L,
            2L,
            MatchParams(
                flowType = FlowProvider.FlowType.VERIFY,
                queryForCandidates = SubjectQuery(subjectId = "subjectId"),
            ),
            2,
            "faceMatcherName",
            listOf(
                FaceMatchResult.Item("guid1", 0.5f),
                FaceMatchResult.Item("guid2", 0.1f)
            ),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(withArg<OneToOneMatchEvent> {
                assertThat(it).isInstanceOf(OneToOneMatchEvent::class.java)
                assertThat(it.payload.createdAt).isEqualTo(1L)
                assertThat(it.payload.endedAt).isEqualTo(2L)
                assertThat(it.payload.candidateId).isEqualTo("subjectId")
                assertThat(it.payload.matcher).isEqualTo("faceMatcherName")
                assertThat(it.payload.result?.candidateId).isEqualTo("guid1")
                assertThat(it.payload.result?.score).isEqualTo(0.5f)
            })
        }
    }

    @Test
    fun `Correctly saves one to many match event`() = runTest {
        useCase.invoke(
            1L,
            2L,
            MatchParams(
                emptyList(),
                emptyList(),
                FlowProvider.FlowType.IDENTIFY,
                SubjectQuery(),
            ),
            2,
            "faceMatcherName",
            listOf(
                FaceMatchResult.Item("guid1", 0.5f),
                FaceMatchResult.Item("guid2", 0.1f)
            ),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(withArg<OneToManyMatchEvent> {
                assertThat(it).isInstanceOf(OneToManyMatchEvent::class.java)
                assertThat(it.payload.createdAt).isEqualTo(1L)
                assertThat(it.payload.endedAt).isEqualTo(2L)
                assertThat(it.payload.matcher).isEqualTo("faceMatcherName")
                assertThat(it.payload.result?.first()?.candidateId).isEqualTo("guid1")
                assertThat(it.payload.result?.last()?.candidateId).isEqualTo("guid2")
            })
        }
    }

    @Test
    fun `Correctly saves one to many match event with USER pool`() = runTest {
        useCase.invoke(
            1L,
            2L,
            MatchParams(
                flowType = FlowProvider.FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(attendantId = "userId"),
            ),
            0,
            "faceMatcherName",
            listOf(FaceMatchResult.Item("guid1", 0.5f)),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(withArg<OneToManyMatchEvent> {
                assertThat(it.payload.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.USER)
            })
        }
    }


    @Test
    fun `Correctly saves one to many match event with MODULE pool`() = runTest {
        useCase.invoke(
            1L,
            2L,
            MatchParams(
                flowType = FlowProvider.FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(moduleId = "moduleId"),
            ),
            0,
            "faceMatcherName",
            emptyList(),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(withArg<OneToManyMatchEvent> {
                assertThat(it.payload.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.MODULE)
            })
        }
    }

    @Test
    fun `Correctly saves one to many match event with PROJECT pool`() = runTest {
        useCase.invoke(
            1L,
            2L,
            MatchParams(
                emptyList(),
                flowType = FlowProvider.FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery()
            ),
            0,
            "faceMatcherName",
            emptyList(),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(withArg<OneToManyMatchEvent> {
                assertThat(it.payload.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT)
            })
        }
    }
}
