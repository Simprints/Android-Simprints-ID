package com.simprints.id.data.db.local.models

import com.google.common.truth.Truth.assertThat
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_MODULE_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.DEFAULT_USER_ID
import com.simprints.eventsystem.sampledata.SampleDefaults.GUID1
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import com.simprints.id.testtools.SubjectsGeneratorUtils.getRandomFaceSample
import com.simprints.id.testtools.SubjectsGeneratorUtils.getRandomFingerprintSample
import org.junit.Test
import java.util.*

class DbSubjectTest {

    @Test
    fun fromDomainToDbModel() {
        val fingerprintSample = getRandomFingerprintSample()
        val faceSample = getRandomFaceSample()

        val domainSubject = Subject(
            GUID1, DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, Date(0), Date(1),
            listOf(fingerprintSample),
            listOf(faceSample)
        )

        val dbSubject = domainSubject.fromDomainToDb()

        with(dbSubject) {
            assertThat(subjectId).isEqualTo(UUID.fromString(GUID1))
            assertThat(attendantId).isEqualTo(DEFAULT_USER_ID)
            assertThat(createdAt).isEqualTo(Date(0))
            assertThat(updatedAt).isEqualTo(Date(1))
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(fingerprintSamples.first()?.id).isEqualTo(fingerprintSample.id)
            assertThat(faceSamples.first()?.id).isEqualTo(faceSample.id)
        }
    }
}
