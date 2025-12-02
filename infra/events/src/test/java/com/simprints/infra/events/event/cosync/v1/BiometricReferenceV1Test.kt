package com.simprints.infra.events.event.cosync.v1

import com.simprints.core.domain.sample.SampleIdentifier
import com.simprints.infra.events.event.domain.models.subject.FaceReference
import com.simprints.infra.events.event.domain.models.subject.FaceTemplate
import com.simprints.infra.events.event.domain.models.subject.FingerprintReference
import com.simprints.infra.events.event.domain.models.subject.FingerprintTemplate
import org.junit.Test
import kotlin.test.assertEquals

class BiometricReferenceV1Test {

    @Test
    fun `face reference toCoSyncV1 and toDomain roundtrip preserves metadata`() {
        // Given: face reference with metadata
        val faceRef = FaceReference(
            id = "face-1",
            templates = listOf(FaceTemplate("template-1")),
            format = "RANK_ONE",
            metadata = mapOf("key1" to "value1", "key2" to "value2"),
        )

        // When: convert to V1 and back
        val v1FaceRef = faceRef.toCoSyncV1()
        val domainFaceRef = v1FaceRef.toDomain()

        // Then: metadata is preserved
        assertEquals(faceRef.id, domainFaceRef.id)
        assertEquals(faceRef.format, domainFaceRef.format)
        assertEquals(faceRef.metadata, domainFaceRef.metadata)
        assertEquals(faceRef.templates.size, domainFaceRef.templates.size)
    }

    @Test
    fun `fingerprint reference toCoSyncV1 and toDomain roundtrip preserves finger identifiers`() {
        // Given: fingerprint reference with multiple templates
        val fingerprintRef = FingerprintReference(
            id = "fingerprint-1",
            templates = listOf(
                FingerprintTemplate("template-1", SampleIdentifier.LEFT_THUMB),
                FingerprintTemplate("template-2", SampleIdentifier.RIGHT_INDEX_FINGER),
            ),
            format = "NEC",
            metadata = null,
        )

        // When: convert to V1 and back
        val v1FingerprintRef = fingerprintRef.toCoSyncV1()
        val domainFingerprintRef = v1FingerprintRef.toDomain()

        // Then: finger identifiers are preserved
        assertEquals(fingerprintRef.id, domainFingerprintRef.id)
        assertEquals(fingerprintRef.format, domainFingerprintRef.format)
        assertEquals(fingerprintRef.metadata, domainFingerprintRef.metadata)
        assertEquals(2, domainFingerprintRef.templates.size)
        assertEquals(SampleIdentifier.LEFT_THUMB, domainFingerprintRef.templates[0].finger)
        assertEquals(SampleIdentifier.RIGHT_INDEX_FINGER, domainFingerprintRef.templates[1].finger)
    }

    @Test
    fun `face reference toCoSyncV1 preserves template data`() {
        // Given: face reference with template
        val faceRef = FaceReference(
            id = "face-2",
            templates = listOf(
                FaceTemplate("base64-template-1"),
                FaceTemplate("base64-template-2"),
            ),
            format = "SIMFACE",
            metadata = null,
        )

        // When: convert to V1
        val v1FaceRef = faceRef.toCoSyncV1()

        // Then: all template data is preserved
        assertEquals(2, v1FaceRef.templates.size)
        assertEquals("base64-template-1", v1FaceRef.templates[0].template)
        assertEquals("base64-template-2", v1FaceRef.templates[1].template)
    }

    @Test
    fun `fingerprint template V1 uses SampleIdentifierV1 type`() {
        // Given: fingerprint reference with domain SampleIdentifier
        val fingerprintRef = FingerprintReference(
            id = "fingerprint-1",
            templates = listOf(FingerprintTemplate("template-1", SampleIdentifier.LEFT_THUMB)),
            format = "NEC",
            metadata = null,
        )

        // When: convert to V1
        val v1FingerprintRef = fingerprintRef.toCoSyncV1()

        // Then: V1 model uses SampleIdentifierV1
        assertEquals(SampleIdentifierV1.LEFT_THUMB, v1FingerprintRef.templates[0].finger)
    }
}
