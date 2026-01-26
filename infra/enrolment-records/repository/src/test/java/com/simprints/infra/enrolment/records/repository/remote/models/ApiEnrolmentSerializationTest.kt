package com.simprints.infra.enrolment.records.repository.remote.models

import com.google.common.truth.Truth.*
import com.simprints.infra.serialization.SimJson
import org.junit.Test

class ApiEnrolmentSerializationTest {
    @Test
    fun `serialization of FaceReference`() {
        val faceRef = ApiFaceReference(
            id = "face_ref_123",
            format = "PNG",
            templates = listOf(ApiFaceTemplate("face_blob")),
        )

        val serialized = SimJson.encodeToString(faceRef)

        assertThat(serialized).contains(ApiBiometricReference.FACE_REFERENCE_KEY)
        assertThat(serialized).contains("face_ref_123")
    }

    @Test
    fun `serialization of FingerprintReference`() {
        val fingerRef = ApiFingerprintReference(
            id = "fp_ref_456",
            format = "ISO",
            templates = listOf(
                ApiFingerprintTemplate("finger_blob", ApiFinger.RIGHT_THUMB),
            ),
        )

        val serialized = SimJson.encodeToString(fingerRef)

        assertThat(serialized).contains(ApiBiometricReference.FINGERPRINT_REFERENCE_KEY)
        assertThat(serialized).contains("fp_ref_456")
    }

    @Test
    fun `ApiFinger enum serializes to expected string`() {
        val template = ApiFingerprintTemplate("blob", ApiFinger.LEFT_THUMB)
        val serialized = SimJson.encodeToString(template)

        assertThat(serialized).contains("LEFT_THUMB")

        val deserialized = SimJson.decodeFromString<ApiFingerprintTemplate>(serialized)
        assertThat(deserialized.finger).isEqualTo(ApiFinger.LEFT_THUMB)
    }

    @Test
    fun `serialize ApiEnrolmentRecord`() {
        val faceRef = ApiFaceReference(
            id = "face_1",
            templates = listOf(ApiFaceTemplate("face_blob")),
            format = "proprietary",
        )
        val fingerRef = ApiFingerprintReference(
            id = "finger_1",
            templates = listOf(ApiFingerprintTemplate("finger_blob", ApiFinger.LEFT_INDEX_FINGER)),
            format = "proprietary",
        )

        val enrollmentRecord = ApiEnrolmentRecord(
            subjectId = "sub_001",
            moduleId = "mod_A",
            attendantId = "att_B",
            biometricReferences = listOf(faceRef, fingerRef),
        )

        val serialized = SimJson.encodeToString(enrollmentRecord)
        // Verify that the serialized string contains one face and one fingerprint reference
        assertThat(serialized).contains(ApiBiometricReference.FACE_REFERENCE_KEY)
        assertThat(serialized).contains(ApiBiometricReference.FINGERPRINT_REFERENCE_KEY)
    }
}
