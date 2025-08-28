package com.simprints.fingerprint.infra.biosdkimpl.matching

import com.google.common.truth.Truth
import com.simprints.core.domain.sample.CaptureIdentity
import com.simprints.core.domain.sample.Identity
import com.simprints.fingerprint.infra.basebiosdk.matching.domain.MatchResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FingerprintMatcherImplTest {
    private var simAfisMatcher: SimAfisMatcher = mockk()

    @Test
    fun match() = runTest {
        // Given
        val matcher = FingerprintMatcherImpl(simAfisMatcher)
        val probe: CaptureIdentity = mockk()
        val candidates: List<Identity> = mockk()
        val simAfisMatcherSettings = SimAfisMatcherSettings()
        simAfisMatcherSettings.crossFingerComparison = false
        val matchResult: List<MatchResult> = mockk()
        every {
            simAfisMatcher.match(probe, candidates, false)
        } returns matchResult

        // When
        val result = matcher.match(probe, candidates, simAfisMatcherSettings)

        // Then
        Truth.assertThat(result).isEqualTo(matchResult)
    }
}
