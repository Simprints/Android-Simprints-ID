package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.google.common.truth.*
import com.simprints.core.domain.reference.BiometricReferenceCapture
import com.simprints.core.domain.sample.ComparisonResult
import com.simprints.core.domain.sample.Identity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FingerprintMatcherImplTest {
    private var simAfisMatcher: SimAfisMatcher = mockk()

    @Test
    fun match() = runTest {
        // Given
        val matcher = FingerprintMatcherImpl(simAfisMatcher)
        val probe = mockk<BiometricReferenceCapture>()
        val candidates: List<Identity> = mockk()
        val simAfisMatcherSettings = SimAfisMatcherSettings()
        simAfisMatcherSettings.crossFingerComparison = false
        val matchResult: List<ComparisonResult> = mockk()
        every {
            simAfisMatcher.match(probe, candidates, false)
        } returns matchResult

        // When
        val result = matcher.match(probe, candidates, simAfisMatcherSettings)

        // Then
        Truth.assertThat(result).isEqualTo(matchResult)
    }
}
