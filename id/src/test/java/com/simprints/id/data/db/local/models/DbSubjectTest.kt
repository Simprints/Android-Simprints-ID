package com.simprints.id.data.db.local.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.sampledata.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.SubjectsGeneratorUtils.getRandomFaceSample
import com.simprints.id.commontesttools.SubjectsGeneratorUtils.getRandomFingerprintSample
import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.data.db.subject.local.models.fromDomainToDb
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

class DbSubjectTest {

    @Test
    fun fromDomainToDbModel() {
        val fingerprintSample = getRandomFingerprintSample()
        val faceSample = getRandomFaceSample()

        val domainSubject = Subject(
            "guid", DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, Date(0), Date(1),
            listOf(fingerprintSample),
            listOf(faceSample)
        )

        val dbSubject = domainSubject.fromDomainToDb()

        with(dbSubject) {
            assertThat(subjectId).isEqualTo("guid")
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
