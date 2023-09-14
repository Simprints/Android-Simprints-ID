package com.simprints.infra.enrolment.records.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.tokenization.asTokenizedEncrypted
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import org.junit.Test
import java.util.Date
import java.util.UUID
import kotlin.random.Random

class DbSubjectTest {

    companion object {
        private const val GUID = "3f0f8e9a-0a0c-456c-846e-577b1440b6fb"
        private const val PROJECT_ID = "projectId"
        private val ATTENDANT_ID = "user1".asTokenizedEncrypted()
        private val MODULE_ID = "module".asTokenizedEncrypted()
    }

    @Test
    fun fromDomainToDbModel() {
        val fingerprintSample = FingerprintSample(
            IFingerIdentifier.RIGHT_3RD_FINGER,
            Random.nextBytes(64),
            30,
            "NEC_1",
        )
        val faceSample = FaceSample(Random.nextBytes(64), "RANK_ONE_1_23")

        val domainSubject = Subject(
            subjectId = GUID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            createdAt = Date(0),
            updatedAt = Date(1),
            fingerprintSamples = listOf(fingerprintSample),
            faceSamples = listOf(faceSample)
        )

        val dbSubject = domainSubject.fromDomainToDb()

        with(dbSubject) {
            assertThat(subjectId).isEqualTo(UUID.fromString(GUID))
            assertThat(attendantId).isEqualTo(ATTENDANT_ID.value)
            assertThat(createdAt).isEqualTo(Date(0))
            assertThat(updatedAt).isEqualTo(Date(1))
            assertThat(moduleId).isEqualTo(MODULE_ID.value)
            assertThat(projectId).isEqualTo(PROJECT_ID)
            assertThat(fingerprintSamples.first()?.id).isEqualTo(fingerprintSample.id)
            assertThat(faceSamples.first()?.id).isEqualTo(faceSample.id)
        }
    }
}
