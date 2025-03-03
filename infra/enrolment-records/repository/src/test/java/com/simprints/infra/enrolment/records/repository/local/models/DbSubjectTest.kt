package com.simprints.infra.enrolment.records.repository.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.enrolment.records.realm.store.models.DbFaceSample
import com.simprints.infra.enrolment.records.realm.store.models.DbFingerprintSample
import com.simprints.infra.enrolment.records.realm.store.models.DbSubject
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmUUID
import org.junit.Test
import java.util.Date
import kotlin.random.Random

class DbSubjectTest {
    companion object {
        private const val GUID = "3f0f8e9a-0a0c-456c-846e-577b1440b6fb"
        private const val PROJECT_ID = "projectId"
        private const val REFERENCE_ID = "referenceId"
        private val ATTENDANT_ID = "user1".asTokenizableEncrypted()
        private val MODULE_ID = "module".asTokenizableEncrypted()
    }

    @Test
    fun fromDomainToDbModel() {
        val fingerprintSample = FingerprintSample(
            IFingerIdentifier.RIGHT_3RD_FINGER,
            Random.nextBytes(64),
            30,
            "NEC_1",
            REFERENCE_ID,
        )
        val faceSample = FaceSample(Random.nextBytes(64), "RANK_ONE_1_23", REFERENCE_ID)

        val domainSubject = Subject(
            subjectId = GUID,
            projectId = PROJECT_ID,
            attendantId = ATTENDANT_ID,
            moduleId = MODULE_ID,
            createdAt = Date(0),
            updatedAt = Date(1500),
            fingerprintSamples = listOf(fingerprintSample),
            faceSamples = listOf(faceSample),
        )

        val dbSubject = domainSubject.fromDomainToDb()

        with(dbSubject) {
            assertThat(subjectId).isEqualTo(RealmUUID.from(GUID))
            assertThat(projectId).isEqualTo(PROJECT_ID)
            assertThat(attendantId).isEqualTo(ATTENDANT_ID.value)
            assertThat(moduleId).isEqualTo(MODULE_ID.value)
            assertThat(createdAt).isEqualTo(RealmInstant.from(0, 0))
            assertThat(updatedAt).isEqualTo(RealmInstant.from(1, 500_000_000))
            assertThat(fingerprintSamples.first().id).isEqualTo(fingerprintSample.id)
            assertThat(fingerprintSamples.first().referenceId).isEqualTo(REFERENCE_ID)
            assertThat(faceSamples.first().id).isEqualTo(faceSample.id)
            assertThat(faceSamples.first().referenceId).isEqualTo(REFERENCE_ID)
        }
    }

    @Test
    fun fromDbModelToDomain() {
        val fingerprintSample = DbFingerprintSample().apply {
            fingerIdentifier = IFingerIdentifier.RIGHT_3RD_FINGER.ordinal
            template = Random.nextBytes(64)
            templateQualityScore = 30
            format = "NEC_1"
            referenceId = REFERENCE_ID
        }
        val faceSample = DbFaceSample().apply {
            template = Random.nextBytes(64)
            format = "RANK_ONE_1_23"
            referenceId = REFERENCE_ID
        }

        val dbSubject = DbSubject().apply {
            subjectId = RealmUUID.Companion.from(GUID)
            attendantId = ATTENDANT_ID.value
            moduleId = MODULE_ID.value
            projectId = PROJECT_ID
            createdAt = RealmInstant.from(0, 0)
            updatedAt = RealmInstant.from(1, 500_000_000)
            faceSamples = realmListOf(faceSample)
            fingerprintSamples = realmListOf(fingerprintSample)
            isModuleIdTokenized = true
            isAttendantIdTokenized = true
        }

        val domainSubject = dbSubject.fromDbToDomain()

        with(domainSubject) {
            assertThat(subjectId).isEqualTo(GUID)
            assertThat(attendantId).isEqualTo(ATTENDANT_ID)
            assertThat(createdAt).isEqualTo(Date(0))
            assertThat(updatedAt).isEqualTo(Date(1500))
            assertThat(moduleId).isEqualTo(MODULE_ID)
            assertThat(projectId).isEqualTo(PROJECT_ID)
            assertThat(fingerprintSamples.first().id).isEqualTo(fingerprintSample.id)
            assertThat(fingerprintSamples.first().referenceId).isEqualTo(REFERENCE_ID)
            assertThat(faceSamples.first().referenceId).isEqualTo(REFERENCE_ID)
        }
    }
}
