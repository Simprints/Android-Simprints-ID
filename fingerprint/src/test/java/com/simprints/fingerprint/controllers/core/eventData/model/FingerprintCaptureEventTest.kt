package com.simprints.fingerprint.controllers.core.eventData.model

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.activities.collect.state.CaptureState
import com.simprints.fingerprint.activities.collect.state.ScanResult
import com.simprints.fingerprint.controllers.core.eventData.model.FingerprintCaptureEvent.Companion.buildResult
import com.simprints.fingerprint.data.domain.fingerprint.FingerIdentifier
import org.junit.Test
import com.simprints.eventsystem.event.domain.models.fingerprint.FingerprintCaptureEventV3 as FingerprintCaptureEventCore
import com.simprints.id.data.db.subject.domain.FingerIdentifier as FingerIdentifierCore

class FingerprintCaptureEventTest {

    @Test
    fun `mapping capture state to result works as expected`() {
        listOf(
            CaptureState.Skipped, CaptureState.NotDetected(), CaptureState.Collected(
                ScanResult(
                    qualityScore = 1,
                    template = byteArrayOf(),
                    image = null,
                    qualityThreshold = 0
                )
            ), CaptureState.Collected(
                ScanResult(
                    qualityScore = 0,
                    template = byteArrayOf(),
                    image = null,
                    qualityThreshold = 2
                )
            )
        ).zip(
            listOf(
                FingerprintCaptureEvent.Result.SKIPPED,
                FingerprintCaptureEvent.Result.NO_FINGER_DETECTED,
                FingerprintCaptureEvent.Result.GOOD_SCAN,
                FingerprintCaptureEvent.Result.BAD_QUALITY
            )
        ).forEach {
            assertThat(buildResult(it.first)).isEqualTo(it.second)
        }
    }

    @Test
    fun `mapping from domain to core`() {
        val domain = FingerprintCaptureEvent(
            startTime = 0,
            endTime = 0,
            finger = FingerIdentifier.LEFT_3RD_FINGER,
            qualityThreshold = 0,
            result = FingerprintCaptureEvent.Result.BAD_QUALITY,
            fingerprint = null
        )

        val core = domain.fromDomainToCore()

        assertThat(domain.fingerprint).isNull()
        assertThat(domain.fingerprint).isEqualTo(core.payload.fingerprint)
        assertThat(core.payload.result).isInstanceOf(FingerprintCaptureEventCore.FingerprintCapturePayloadV3.Result.BAD_QUALITY::class.java)
    }

    @Test
    fun `mapping fingerprints works as expected`() {
        listOf(
            FingerIdentifier.RIGHT_5TH_FINGER,
            FingerIdentifier.RIGHT_4TH_FINGER,
            FingerIdentifier.RIGHT_3RD_FINGER,
            FingerIdentifier.RIGHT_INDEX_FINGER,
            FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER,
            FingerIdentifier.LEFT_3RD_FINGER,
            FingerIdentifier.LEFT_4TH_FINGER,
            FingerIdentifier.LEFT_5TH_FINGER
        ).zip(
            listOf(
                FingerIdentifierCore.RIGHT_5TH_FINGER,
                FingerIdentifierCore.RIGHT_4TH_FINGER,
                FingerIdentifierCore.RIGHT_3RD_FINGER,
                FingerIdentifierCore.RIGHT_INDEX_FINGER,
                FingerIdentifierCore.RIGHT_THUMB,
                FingerIdentifierCore.LEFT_THUMB,
                FingerIdentifierCore.LEFT_INDEX_FINGER,
                FingerIdentifierCore.LEFT_3RD_FINGER,
                FingerIdentifierCore.LEFT_4TH_FINGER,
                FingerIdentifierCore.LEFT_5TH_FINGER
            )
        ).forEach {
            assertThat(it.first.fromDomainToCore()).isEqualTo(it.second)
        }
    }
}
