package com.simprints.infra.enrolment.records.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.infra.enrolment.records.domain.models.Subject
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat
import com.simprints.moduleapi.fingerprint.IFingerIdentifier
import com.simprints.moduleapi.fingerprint.IFingerprintTemplateFormat
import org.junit.Test
import java.util.*
import kotlin.random.Random

class DbSubjectTest {

    companion object {
        private const val GUID = "3f0f8e9a-0a0c-456c-846e-577b1440b6fb"
        private const val PROJECT_ID = "projectId"
        private const val ATTENDANT_ID = "user1"
        private const val MODULE_ID = "module"
    }

    @Test
    fun fromDomainToDbModel() {
        val fingerprintSample = FingerprintSample(
            IFingerIdentifier.RIGHT_3RD_FINGER,
            Random.nextBytes(64),
            30,
            IFingerprintTemplateFormat.NEC_1,
        )
        val faceSample = FaceSample(Random.nextBytes(64), IFaceTemplateFormat.RANK_ONE_1_23)

        val domainSubject = Subject(
            GUID, PROJECT_ID, ATTENDANT_ID, MODULE_ID, Date(0), Date(1),
            listOf(fingerprintSample),
            listOf(faceSample)
        )

        val dbSubject = domainSubject.fromDomainToDb()

        with(dbSubject) {
            assertThat(subjectId).isEqualTo(UUID.fromString(GUID))
            assertThat(attendantId).isEqualTo(ATTENDANT_ID)
            assertThat(createdAt).isEqualTo(Date(0))
            assertThat(updatedAt).isEqualTo(Date(1))
            assertThat(moduleId).isEqualTo(MODULE_ID)
            assertThat(projectId).isEqualTo(PROJECT_ID)
            assertThat(fingerprintSamples.first()?.id).isEqualTo(fingerprintSample.id)
            assertThat(faceSamples.first()?.id).isEqualTo(faceSample.id)
        }
    }
}
