package com.simprints.matcher.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.common.FlowType
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.fingerprint.infra.biosdk.BioSdkWrapper
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.enrolment.records.sync.EnrolmentRecordManager
import com.simprints.matcher.MatchParams
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class FingerprintMatcherUseCaseTest {

    @MockK
    lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    lateinit var bioSdkWrapper: BioSdkWrapper

    @MockK
    lateinit var configManager: ConfigManager

    private lateinit var useCase: FingerprintMatcherUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        useCase = FingerprintMatcherUseCase(enrolmentRecordManager, bioSdkWrapper, configManager)
    }

    @Test
    fun `Skips matching if there are no probes`() = runTest {
        coEvery { enrolmentRecordManager.loadFaceIdentities(any()) } returns emptyList()
        coEvery { bioSdkWrapper.match(any(), any(), any()) } returns listOf()

        useCase.invoke(
            MatchParams(
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
        )

        coVerify(exactly = 0) { bioSdkWrapper.match(any(), any(), any()) }
    }

    @Test
    fun `Correctly calls SDK matcher`() = runTest {
        coEvery { enrolmentRecordManager.loadFingerprintIdentities(any()) } returns listOf(
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
        var onMatchingCalled = false

        useCase.invoke(
            MatchParams(
                probeFingerprintSamples = listOf(
                    MatchParams.FingerprintSample(
                        IFingerIdentifier.LEFT_3RD_FINGER,
                        "format",
                        byteArrayOf(1, 2, 3)
                    ),
                ),
                flowType = FlowType.VERIFY,
                queryForCandidates = SubjectQuery()
            ),
            { onLoadingCalled = true },
            { onMatchingCalled = true }
        )

        coVerify { bioSdkWrapper.match(any(), any(), any()) }

        assertThat(onLoadingCalled).isTrue()
        assertThat(onMatchingCalled).isTrue()
    }

    private fun fingerprintSample(finger: IFingerIdentifier) =
        FingerprintSample(finger, byteArrayOf(1), 42, "format")
}
