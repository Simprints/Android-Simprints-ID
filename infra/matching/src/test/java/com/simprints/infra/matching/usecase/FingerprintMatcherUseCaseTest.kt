package com.simprints.infra.matching.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.BiometricTemplateCapture
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.infra.config.store.models.FaceConfiguration
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.CandidateRecordBatch
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.matching.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class FingerprintMatcherUseCaseTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    lateinit var timeHelper: TimeHelper

    @MockK
    lateinit var enrolmentRecordRepository: EnrolmentRecordRepository

    @MockK
    lateinit var bioSdkWrapper: BioSdkWrapper

    @MockK
    lateinit var resolveBioSdkWrapperUseCase: ResolveBioSdkWrapperUseCase

    @MockK
    lateinit var configManager: ConfigManager

    @MockK
    lateinit var project: Project

    @MockK
    lateinit var createRangesUseCase: CreateRangesUseCase
    private lateinit var useCase: FingerprintMatcherUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { resolveBioSdkWrapperUseCase(any()) } returns bioSdkWrapper
        coEvery {
            configManager.getProjectConfiguration().fingerprint?.allowedSDKs
        } returns listOf(SECUGEN_SIM_MATCHER)

        useCase = FingerprintMatcherUseCase(
            timeHelper,
            enrolmentRecordRepository,
            resolveBioSdkWrapperUseCase,
            configManager,
            createRangesUseCase,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    @Test
    fun `Skips matching if there are no probes`() = runTest {
        val results = useCase
            .invoke(
                MatchParams(
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FINGERPRINT,
                        format = "format",
                        templates = emptyList(),
                    ),
                    bioSdk = SECUGEN_SIM_MATCHER,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                comparisonResults = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )
    }

    @Test
    fun `Skips matching if there are no candidates`() = runTest {
        coEvery { enrolmentRecordRepository.count(any()) } returns 0
        coEvery {
            enrolmentRecordRepository.loadCandidateRecords(any(), any(), any(), project, any(), any())
        } returns createTestChannel(emptyList())
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        val results = useCase
            .invoke(
                MatchParams(
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FINGERPRINT,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "fingerprintId",
                                template = byteArrayOf(1, 2, 3),
                                identifier = TemplateIdentifier.LEFT_3RD_FINGER,
                            ),
                        ),
                    ),
                    bioSdk = SECUGEN_SIM_MATCHER,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                comparisonResults = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )
    }

    @Test
    fun `Logs warning and returns empty success when wrong SDK type is provided`() = runTest {
        mockkObject(Simber)
        justRun { Simber.w(message = any<String>(), t = any<Throwable>(), tag = any<String>()) }

        val results = useCase
            .invoke(
                MatchParams(
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FINGERPRINT,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "fingerprintId",
                                template = byteArrayOf(1, 2, 3),
                                identifier = TemplateIdentifier.LEFT_3RD_FINGER,
                            ),
                        ),
                    ),
                    bioSdk = FaceConfiguration.BioSdk.RANK_ONE, // Wrong SDK type
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        verify {
            Simber.w(
                message = "Fingerprint SDK was not provided",
                t = ofType<IllegalArgumentException>(),
                tag = LoggingConstants.CrashReportTag.FINGER_MATCHING,
            )
        }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                comparisonResults = emptyList(),
                totalCandidates = 0,
                matcherName = "",
                matchBatches = emptyList(),
            ),
        )

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }
        unmockkObject(Simber)
    }

    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        coEvery { enrolmentRecordRepository.count(any(), any()) } returns 100
        coEvery { createRangesUseCase(any()) } returns listOf(0..99)
        coEvery {
            enrolmentRecordRepository.loadCandidateRecords(
                any(),
                any(),
                any(),
                project,
                any(),
                any(),
            )
        } returns createTestChannel(
            listOf(
                CandidateRecord(
                    "personId",
                    listOf(
                        fingerprintReference(TemplateIdentifier.RIGHT_5TH_FINGER),
                        fingerprintReference(TemplateIdentifier.RIGHT_4TH_FINGER),
                        fingerprintReference(TemplateIdentifier.RIGHT_3RD_FINGER),
                        fingerprintReference(TemplateIdentifier.RIGHT_INDEX_FINGER),
                        fingerprintReference(TemplateIdentifier.RIGHT_THUMB),
                        fingerprintReference(TemplateIdentifier.LEFT_THUMB),
                        fingerprintReference(TemplateIdentifier.LEFT_INDEX_FINGER),
                        fingerprintReference(TemplateIdentifier.LEFT_3RD_FINGER),
                        fingerprintReference(TemplateIdentifier.LEFT_4TH_FINGER),
                        fingerprintReference(TemplateIdentifier.LEFT_5TH_FINGER),
                    ),
                ),
            ),
        )
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        useCase
            .invoke(
                matchParams = MatchParams(
                    probeReference = BiometricReferenceCapture(
                        referenceId = "referenceId",
                        modality = Modality.FINGERPRINT,
                        format = "format",
                        templates = listOf(
                            BiometricTemplateCapture(
                                captureEventId = "fingerprintId",
                                template = byteArrayOf(1, 2, 3),
                                identifier = TemplateIdentifier.LEFT_3RD_FINGER,
                            ),
                        ),
                    ),
                    bioSdk = SECUGEN_SIM_MATCHER,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = EnrolmentRecordQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()
        coVerify { bioSdkWrapper.match(any(), any(), any()) }
    }

    private fun fingerprintReference(finger: TemplateIdentifier) = BiometricReference(
        templates = listOf(
            BiometricTemplate(
                identifier = finger,
                template = byteArrayOf(1),
            ),
        ),
        format = "format",
        referenceId = "referenceId",
        modality = Modality.FINGERPRINT,
    )
}

fun createTestChannel(vararg lists: List<CandidateRecord>): ReceiveChannel<CandidateRecordBatch> {
    val channel = Channel<CandidateRecordBatch>(lists.size)
    runBlocking {
        var time = 0L
        for (list in lists) {
            channel.send(
                CandidateRecordBatch(
                    identities = list,
                    loadingStartTime = Timestamp(time++),
                    loadingEndTime = Timestamp(time++),
                ),
            )
        }
        channel.close()
    }
    return channel
}
