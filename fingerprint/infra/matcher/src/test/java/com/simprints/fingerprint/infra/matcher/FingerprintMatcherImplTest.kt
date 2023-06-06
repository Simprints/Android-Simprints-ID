package com.simprints.fingerprint.infra.matcher

import com.google.common.truth.Truth
import com.simprints.fingerprint.infra.matcher.algorithms.simafis.SimAfisMatcher
import com.simprints.fingerprint.infra.matcher.domain.FingerprintIdentity
import com.simprints.fingerprint.infra.matcher.domain.MatchResult
import com.simprints.fingerprint.infra.matcher.domain.MatchingAlgorithm
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class FingerprintMatcherImplTest {

    private var simAfisMatcher: SimAfisMatcher = mockk()

    @Test
    fun match() = runBlocking {
        // Given
        val matcher = FingerprintMatcherImpl(simAfisMatcher)
        val probe: FingerprintIdentity = mockk()
        val candidates: List<FingerprintIdentity> = mockk()
        val matchingAlgorithm = MatchingAlgorithm.SIM_AFIS
        val crossFingerComparison = false
        val matchResult: List<MatchResult> = mockk()
        every {
            simAfisMatcher.match(probe, candidates, crossFingerComparison)
        } returns matchResult

        // When
        val result = matcher.match(probe, candidates, matchingAlgorithm, crossFingerComparison)

        // Then
        Truth.assertThat(result).isEqualTo(matchResult)
    }
}
