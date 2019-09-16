package com.simprints.id.data.db.local.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_MODULE_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_PROJECT_ID
import com.simprints.id.commontesttools.DefaultTestConstants.DEFAULT_USER_ID
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomFaceSample
import com.simprints.id.commontesttools.PeopleGeneratorUtils.getRandomFingerprintSample
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.data.db.person.local.models.fromDomainToDb
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class DbPersonTest {

    @Test
    fun fromDomainToDbModel() {
        val fingerprintSample = getRandomFingerprintSample()
        val faceSample = getRandomFaceSample()

        val domainPerson = Person(
            "guid", DEFAULT_PROJECT_ID, DEFAULT_USER_ID, DEFAULT_MODULE_ID, Date(0), Date(1), true,
            listOf(fingerprintSample),
            listOf(faceSample)
        )

        val dbPerson = domainPerson.fromDomainToDb()

        with(dbPerson) {
            assertThat(patientId).isEqualTo("guid")
            assertThat(userId).isEqualTo(DEFAULT_USER_ID)
            assertThat(createdAt).isEqualTo(Date(0))
            assertThat(updatedAt).isEqualTo(Date(1))
            assertThat(moduleId).isEqualTo(DEFAULT_MODULE_ID)
            assertThat(projectId).isEqualTo(DEFAULT_PROJECT_ID)
            assertThat(fingerprintSamples.first()?.id).isEqualTo(fingerprintSample.id)
            assertThat(faceSamples.first()?.id).isEqualTo(faceSample.id)
            assertThat(toSync).isTrue()
        }
    }
}
