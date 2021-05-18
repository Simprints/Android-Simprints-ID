package com.simprints.id.data.prefs.settings.fingerprint.serializers

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import org.junit.Test

class FingerprintsToCollectSerializerTest {

    private val fingerprintsToCollectSerializer = FingerprintsToCollectSerializer()

    @Test
    fun serializesCorrectly() {
        assertThat(fingerprintsToCollectSerializer.serialize(
            listOf(FingerIdentifier.LEFT_THUMB)
        )).isEqualTo(
            "LEFT_THUMB"
        )

        assertThat(fingerprintsToCollectSerializer.serialize(
            listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER, FingerIdentifier.RIGHT_THUMB)
        )).isEqualTo(
            "LEFT_THUMB,LEFT_INDEX_FINGER,RIGHT_THUMB"
        )

        assertThat(fingerprintsToCollectSerializer.serialize(
            listOf()
        )).isEqualTo(
            ""
        )
    }

    @Test
    fun deserializesCorrectly() {
        assertThat(fingerprintsToCollectSerializer.deserialize(
            "LEFT_THUMB"
        )).isEqualTo(
            listOf(FingerIdentifier.LEFT_THUMB)
        )

        assertThat(fingerprintsToCollectSerializer.deserialize(
            "LEFT_THUMB,LEFT_INDEX_FINGER,RIGHT_THUMB"
        )).isEqualTo(
            listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER, FingerIdentifier.RIGHT_THUMB)
        )

        assertThat(fingerprintsToCollectSerializer.deserialize(
            ""
        )).isEqualTo(
            listOf<FingerIdentifier>()
        )
    }
}
