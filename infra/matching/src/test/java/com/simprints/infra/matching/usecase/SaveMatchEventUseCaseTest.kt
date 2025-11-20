package com.simprints.infra.matching.usecase

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.sample.CaptureSample
import com.simprints.core.domain.sample.MatchComparisonResult
import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.config.store.models.FaceConfiguration
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
import com.simprints.infra.matching.MatchBatchInfo
import com.simprints.infra.matching.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
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
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                queryForCandidates = SubjectQuery(subjectId = "subjectId"),
                probeFaceSamples = listOf(
                    CaptureSample(
                        captureEventId = "faceId",
                        template = byteArrayOf(1, 2, 3),
                        modality = Modality.FACE,
                        format = "format",
                    ),
                ),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            2,
            "faceMatcherName",
            listOf(
                MatchComparisonResult("guid1", 0.5f),
                MatchComparisonResult("guid2", 0.1f),
            ),
            batches = emptyList(),
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
                    CaptureSample(
                        captureEventId = "fingerprintId",
                        template = byteArrayOf(1, 2, 3),
                        modality = Modality.FINGERPRINT,
                        format = "format",
                        identifier = SampleIdentifier.RIGHT_5TH_FINGER,
                    ),
                ),
                bioSdk = SECUGEN_SIM_MATCHER,
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            2,
            "faceMatcherName",
            listOf(
                MatchComparisonResult("guid1", 0.5f),
                MatchComparisonResult("guid2", 0.1f),
            ),
            batches = emptyList(),
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
        val batches = listOf(
            MatchBatchInfo(Timestamp(3L), Timestamp(4L), Timestamp(5L), Timestamp(6L), 10),
            MatchBatchInfo(Timestamp(7L), Timestamp(8L), Timestamp(9L), Timestamp(10L), 5),
        )
        useCase.invoke(
            startTime = Timestamp(1L),
            endTime = Timestamp(2L),
            matchParams = MatchParams(
                probeReferenceId = "referenceId",
                probeFaceSamples = emptyList(),
                probeFingerprintSamples = emptyList(),
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                flowType = FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            candidatesCount = 2,
            matcherName = "faceMatcherName",
            results = listOf(
                MatchComparisonResult("guid1", 0.5f),
                MatchComparisonResult("guid2", 0.1f),
            ),
            batches = batches,
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
                    val payloadV3 = it.payload as OneToManyMatchPayloadV3
                    assertThat(payloadV3.probeBiometricReferenceId).isEqualTo("referenceId")
                    assertThat(payloadV3.batches!!).hasSize(2)
                    assertThat(payloadV3.batches!![0].loadingStartTime).isEqualTo(Timestamp(3L))
                    assertThat(payloadV3.batches!![0].loadingEndTime).isEqualTo(Timestamp(4L))
                    assertThat(payloadV3.batches!![0].comparingStartTime).isEqualTo(Timestamp(5L))
                    assertThat(payloadV3.batches!![0].comparingEndTime).isEqualTo(Timestamp(6L))
                    assertThat(payloadV3.batches!![0].count).isEqualTo(10)
                    assertThat(payloadV3.batches!![1].loadingStartTime).isEqualTo(Timestamp(7L))
                    assertThat(payloadV3.batches!![1].loadingEndTime).isEqualTo(Timestamp(8L))
                    assertThat(payloadV3.batches!![1].comparingStartTime).isEqualTo(Timestamp(9L))
                    assertThat(payloadV3.batches!![1].comparingEndTime).isEqualTo(Timestamp(10L))
                    assertThat(payloadV3.batches!![1].count).isEqualTo(5)
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event with USER pool`() = runTest {
        val batches = listOf(
            MatchBatchInfo(Timestamp(3L), Timestamp(4L), Timestamp(5L), Timestamp(6L), 10),
        )
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                flowType = FlowType.IDENTIFY,
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                queryForCandidates = SubjectQuery(attendantId = "userId".asTokenizableEncrypted()),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            0,
            "faceMatcherName",
            listOf(MatchComparisonResult("guid1", 0.5f)),
            batches = batches,
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    val payloadV3 = it.payload as OneToManyMatchPayloadV3
                    assertThat(payloadV3.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.USER)
                    assertThat(payloadV3.batches!!).hasSize(1)
                    assertThat(payloadV3.batches!![0].loadingStartTime).isEqualTo(Timestamp(3L))
                    assertThat(payloadV3.batches!![0].loadingEndTime).isEqualTo(Timestamp(4L))
                    assertThat(payloadV3.batches!![0].comparingStartTime).isEqualTo(Timestamp(5L))
                    assertThat(payloadV3.batches!![0].comparingEndTime).isEqualTo(Timestamp(6L))
                    assertThat(payloadV3.batches!![0].count).isEqualTo(10)
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event with MODULE pool`() = runTest {
        val batches = listOf(
            MatchBatchInfo(Timestamp(3L), Timestamp(4L), Timestamp(5L), Timestamp(6L), 10),
        )
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                flowType = FlowType.IDENTIFY,
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                queryForCandidates = SubjectQuery(moduleId = "moduleId".asTokenizableEncrypted()),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            0,
            "faceMatcherName",
            emptyList(),
            batches = batches,
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    val payloadV3 = it.payload as OneToManyMatchPayloadV3
                    assertThat(payloadV3.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.MODULE)
                    assertThat(payloadV3.batches!!).hasSize(1)
                    assertThat(payloadV3.batches!![0].loadingStartTime).isEqualTo(Timestamp(3L))
                    assertThat(payloadV3.batches!![0].loadingEndTime).isEqualTo(Timestamp(4L))
                    assertThat(payloadV3.batches!![0].comparingStartTime).isEqualTo(Timestamp(5L))
                    assertThat(payloadV3.batches!![0].comparingEndTime).isEqualTo(Timestamp(6L))
                    assertThat(payloadV3.batches!![0].count).isEqualTo(10)
                },
            )
        }
    }

    @Test
    fun `Correctly saves one to many match event with PROJECT pool`() = runTest {
        val batches = listOf(
            MatchBatchInfo(Timestamp(3L), Timestamp(4L), Timestamp(5L), Timestamp(6L), 10),
        )
        useCase.invoke(
            Timestamp(1L),
            Timestamp(2L),
            MatchParams(
                probeReferenceId = "referenceId",
                bioSdk = FaceConfiguration.BioSdk.RANK_ONE,
                probeFaceSamples = emptyList(),
                flowType = FlowType.IDENTIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            0,
            "faceMatcherName",
            emptyList(),
            batches = batches,
        )

        // Then
        coVerify {
            eventRepository.addOrUpdateEvent(
                withArg<OneToManyMatchEvent> {
                    val payloadV3 = it.payload as OneToManyMatchPayloadV3
                    assertThat(payloadV3.pool.type).isEqualTo(OneToManyMatchEvent.OneToManyMatchPayload.MatchPoolType.PROJECT)
                    assertThat(payloadV3.batches!!).hasSize(1)
                    assertThat(payloadV3.batches!![0].loadingStartTime).isEqualTo(Timestamp(3L))
                    assertThat(payloadV3.batches!![0].loadingEndTime).isEqualTo(Timestamp(4L))
                    assertThat(payloadV3.batches!![0].comparingStartTime).isEqualTo(Timestamp(5L))
                    assertThat(payloadV3.batches!![0].comparingEndTime).isEqualTo(Timestamp(6L))
                    assertThat(payloadV3.batches!![0].count).isEqualTo(10)
                },
            )
        }
    }
}
