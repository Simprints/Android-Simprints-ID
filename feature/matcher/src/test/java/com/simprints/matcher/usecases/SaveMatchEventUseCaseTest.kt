package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.FingerprintConfiguration.FingerComparisonStrategy
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent
import com.simprints.infra.events.event.domain.models.OneToManyMatchEvent.OneToManyMatchPayload.OneToManyMatchPayloadV3
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent
import com.simprints.infra.events.event.domain.models.OneToOneMatchEvent.OneToOneMatchPayload.OneToOneMatchPayloadV4
import com.simprints.infra.events.session.SessionEventRepository
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
    private lateinit var eventRepository: SessionEventRepository

    @MockK
    private lateinit var configManager: ConfigManager

    private lateinit var useCase: SaveMatchEventUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        coEvery { eventRepository.addOrUpdateEvent(any()) } just Runs

        coEvery {
            configManager
                .getProjectConfiguration()
                .fingerprint
                ?.getSdkConfiguration(SECUGEN_SIM_MATCHER)
                ?.comparisonStrategyForVerification
        } returns FingerComparisonStrategy.SAME_FINGER

        useCase = SaveMatchEventUseCase(
            eventRepository,
            configManager,
            CoroutineScope(testCoroutineRule.testCoroutineDispatcher),
        )
    }

    @Test
    fun `Correctly saves one to one face match event`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(subjectId = "subjectId"),
                probeFaceSamples = listOf(MatchParams.FaceSample("faceId", byteArrayOf(1, 2, 3))),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            2,
            "faceMatcherName",
            listOf(
                FaceMatchResult.Item("guid1", 0.5f),
                FaceMatchResult.Item("guid2", 0.1f),
            ),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToOneMatchEvent> {
                    assertThat(it).isInstanceOf(OneToOneMatchEvent::class.java)
                    assertThat(it.payload.createdAt).isEqualTo(Timestamp(1L))
                    assertThat(it.payload.endedAt).isEqualTo(Timestamp(2L))
                    assertThat(it.payload.candidateId).isEqualTo("subjectId")
                    assertThat(it.payload.matcher).isEqualTo("faceMatcherName")
                    assertThat(it.payload.result?.candidateId).isEqualTo("guid1")
                    assertThat(it.payload.result?.score).isEqualTo(0.5f)
                    assertThat((it.payload as OneToOneMatchPayloadV4).probeBiometricReferenceId).isEqualTo("referenceId")
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to one fingerprint match event`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(subjectId = "subjectId"),
                probeFingerprintSamples = listOf(
                    MatchParams.FingerprintSample(
                        IFingerIdentifier.RIGHT_5TH_FINGER,
                        "format",
                        byteArrayOf(1, 2, 3),
                    ),
                ),
                fingerprintSDK = SECUGEN_SIM_MATCHER,
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            2,
            "faceMatcherName",
            listOf(
                FaceMatchResult.Item("guid1", 0.5f),
                FaceMatchResult.Item("guid2", 0.1f),
            ),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToOneMatchEvent> {
                    assertThat(it).isInstanceOf(OneToOneMatchEvent::class.java)
                    assertThat(it.payload.createdAt).isEqualTo(Timestamp(1L))
                    assertThat(it.payload.endedAt).isEqualTo(Timestamp(2L))
                    assertThat(it.payload.candidateId).isEqualTo("subjectId")
                    assertThat(it.payload.matcher).isEqualTo("faceMatcherName")
                    assertThat(it.payload.result?.candidateId).isEqualTo("guid1")
                    assertThat(it.payload.result?.score).isEqualTo(0.5f)
                    assertThat((it.payload as OneToOneMatchPayloadV4).probeBiometricReferenceId).isEqualTo("referenceId")
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event`() = runTest {
        useCase.invoke(
            startTime = Timestamp(1L),
            endTime = Timestamp(2L),
            matchParams = MatchParams(
                probeReferenceId = "referenceId",
                probeFaceSamples = emptyList(),
                probeFingerprintSamples = emptyList(),
                fingerprintSDK = null,
                flowType = FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            candidatesCount = 2,
            matcherName = "faceMatcherName",
            results = listOf(
                FaceMatchResult.Item("guid1", 0.5f),
                FaceMatchResult.Item("guid2", 0.1f),
            ),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    assertThat(it).isInstanceOf(OneToManyMatchEvent::class.java)
                    assertThat(it.payload.createdAt).isEqualTo(Timestamp(1L))
                    assertThat(it.payload.endedAt).isEqualTo(Timestamp(2L))
                    assertThat(it.payload.matcher).isEqualTo("faceMatcherName")
                    assertThat(
                        it.payload.result
                            ?.first()
                            ?.candidateId,
                    ).isEqualTo("guid1")
                    assertThat(
                        it.payload.result
                            ?.last()
                            ?.candidateId,
                    ).isEqualTo("guid2")
                    assertThat((it.payload as OneToManyMatchPayloadV3).probeBiometricReferenceId).isEqualTo("referenceId")
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event with USER pool`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                flowType = FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(attendantId = "userId".asTokenizableEncrypted()),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            0,
            "faceMatcherName",
            listOf(FaceMatchResult.Item("guid1", 0.5f)),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    assertThat(it.payload.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.USER)
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event with MODULE pool`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                flowType = FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(moduleId = "moduleId".asTokenizableEncrypted()),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            0,
            "faceMatcherName",
            emptyList(),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    assertThat(it.payload.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.MODULE)
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event with PROJECT pool`() = runTest {
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                emptyList(),
                flowType = FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            0,
            "faceMatcherName",
            emptyList(),
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    assertThat(it.payload.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT)
                },
            )
        }
    }
}
