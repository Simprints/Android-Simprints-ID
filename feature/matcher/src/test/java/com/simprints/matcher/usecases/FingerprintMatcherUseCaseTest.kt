package com.simprints.matcher.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.matcher.MatchParams
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
                    probeReferenceId = "referenceId",
                    probeFingerprintSamples = emptyList(),
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                matchResultItems = emptyList(),
                totalCandidates = 0,
                matcherName = "",
            ),
        )
    }

    @Test
    fun `Skips matching if there are no candidates`() = runTest {
        coEvery { enrolmentRecordRepository.count(any()) } returns 0
        coEvery { enrolmentRecordRepository.loadFingerprintIdentities(any(), any(), any(), project, any(), any()) } returns
            createTestChannel(emptyList())
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        val results = useCase
            .invoke(
                MatchParams(
                    probeReferenceId = "referenceId",
                    probeFingerprintSamples = listOf(
                        MatchParams.FingerprintSample(
                            IFingerIdentifier.LEFT_3RD_FINGER,
                            "format",
                            byteArrayOf(1, 2, 3),
                        ),
                    ),
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }

        assertThat(results).containsExactly(
            MatcherUseCase.MatcherState.Success(
                matchResultItems = emptyList(),
                totalCandidates = 0,
                matcherName = "",
            ),
        )
    }

    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        coEvery { enrolmentRecordRepository.count(any(), any()) } returns 100
        coEvery { createRangesUseCase(any()) } returns listOf(0..99)
        coEvery {
            enrolmentRecordRepository.loadFingerprintIdentities(
                any(),
                any(),
                any(),
                project,
                any(),
                any(),
            )
        } returns
            createTestChannel(
                listOf(
                    FingerprintIdentity(
                        "personId",
                        listOf(
                            fingerprintSample(IFingerIdentifier.RIGHT_5TH_FINGER),
                            fingerprintSample(IFingerIdentifier.RIGHT_4TH_FINGER),
                            fingerprintSample(IFingerIdentifier.RIGHT_3RD_FINGER),
                            fingerprintSample(IFingerIdentifier.RIGHT_INDEX_FINGER),
                            fingerprintSample(IFingerIdentifier.RIGHT_THUMB),
                            fingerprintSample(IFingerIdentifier.LEFT_THUMB),
                            fingerprintSample(IFingerIdentifier.LEFT_INDEX_FINGER),
                            fingerprintSample(IFingerIdentifier.LEFT_3RD_FINGER),
                            fingerprintSample(IFingerIdentifier.LEFT_4TH_FINGER),
                            fingerprintSample(IFingerIdentifier.LEFT_5TH_FINGER),
                        ),
                    ),
                ),
            )
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        useCase
            .invoke(
                matchParams = MatchParams(
                    probeReferenceId = "referenceId",
                    probeFingerprintSamples = listOf(
                        MatchParams.FingerprintSample(
                            IFingerIdentifier.LEFT_3RD_FINGER,
                            "format",
                            byteArrayOf(1, 2, 3),
                        ),
                    ),
                    fingerprintSDK = SECUGEN_SIM_MATCHER,
                    flowType = FlowType.VERIFY,
                    queryForCandidates = SubjectQuery(),
                    biometricDataSource = BiometricDataSource.Simprints,
                ),
                project,
            ).toList()
        coVerify { bioSdkWrapper.match(any(), any(), any()) }
    }

    private fun fingerprintSample(finger: IFingerIdentifier) = FingerprintSample(finger, byteArrayOf(1), "format", "referenceId")
}

fun <T> createTestChannel(vararg lists: List<T>): ReceiveChannel<List<T>> {
    val channel = Channel<List<T>>(lists.size)
    runBlocking {
        for (list in lists) {
            channel.send(list)
        }
        channel.close()
    }
    return channel
}
