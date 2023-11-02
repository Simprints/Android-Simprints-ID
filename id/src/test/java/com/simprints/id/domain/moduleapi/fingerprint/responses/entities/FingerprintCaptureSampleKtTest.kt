package com.simprints.id.domain.moduleapi.fingerprint.responses.entities

import com.google.common.truth.Truth
import com.simprints.infra.config.store.models.Finger
import com.simprints.testtools.unit.EncodingUtilsImplForTests
import org.junit.Test

class FingerprintCaptureSampleKtTest {
    @Test
    fun `test mapping FingerprintCaptureSample to IFingerprintSample`() {
        val fingerprintSample = FingerprintCaptureSample(
            Finger.LEFT_THUMB,
            templateQualityScore = 10,
            template = EncodingUtilsImplForTests.base64ToBytes(
                "sometemplate"
            ),
            format = "ISO_19794_2"
        )
        val fingerprintCaptureSample = fingerprintSample.fromDomainToModuleApi()
        Truth.assertThat(fingerprintCaptureSample.fingerIdentifier.name)
            .isEqualTo(fingerprintSample.fingerIdentifier.name)
        Truth.assertThat(fingerprintCaptureSample.template).isEqualTo(fingerprintSample.template)
        Truth.assertThat(fingerprintCaptureSample.templateQualityScore)
            .isEqualTo(fingerprintSample.templateQualityScore)
        Truth.assertThat(fingerprintCaptureSample.format).isEqualTo(fingerprintSample.format)
    }
}
