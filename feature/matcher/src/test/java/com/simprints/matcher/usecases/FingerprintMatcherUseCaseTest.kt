package com.simprints.matcher.usecases

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.fingerprint.infra.biosdk.ResolveBioSdkWrapperUseCase
import com.simprints.infra.config.store.models.FingerprintConfiguration.BioSdk.SECUGEN_SIM_MATCHER
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.store.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.matcher.MatchParams
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
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
        useCase.invoke(
            MatchParams(
                probeFingerprintSamples = emptyList(),
                fingerprintSDK = SECUGEN_SIM_MATCHER,
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }
    }

    @Test
    fun `Skips matching if there are no candidates`() = runTest {
        coEvery { enrolmentRecordRepository.count(any()) } returns 0
        coEvery { enrolmentRecordRepository.loadFaceIdentities(any(), any()) } returns emptyList()
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        useCase.invoke(
            MatchParams(
                probeFingerprintSamples = listOf(
                    MatchParams.FingerprintSample(
                        IFingerIdentifier.LEFT_3RD_FINGER,
                        "format",
                        byteArrayOf(1, 2, 3)
                    ),
                ),
                fingerprintSDK = SECUGEN_SIM_MATCHER,
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
        )

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }
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
            )
        } returns listOf(
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
                )
            )
        )
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        var onLoadingCalled = false

        useCase.invoke(
            matchParams = MatchParams(
                probeFingerprintSamples = listOf(
                    MatchParams.FingerprintSample(
                        IFingerIdentifier.LEFT_3RD_FINGER,
                        "format",
                        byteArrayOf(1, 2, 3)
                    ),
                ),
                fingerprintSDK = SECUGEN_SIM_MATCHER,
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery(),
                biometricDataSource = BiometricDataSource.Simprints,
            ),
            onLoadingCandidates = { onLoadingCalled = true },
        )

        coVerify { bioSdkWrapper.match(any(), any(), any()) }

        assertThat(onLoadingCalled).isTrue()
    }

    private fun fingerprintSample(finger: IFingerIdentifier) =
        FingerprintSample(finger, byteArrayOf(1), 42, "format")
}
