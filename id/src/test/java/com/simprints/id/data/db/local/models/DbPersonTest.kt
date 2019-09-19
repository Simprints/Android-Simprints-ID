package com.simprints.id.data.db.local.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.commontesttools.FingerprintGeneratorUtils
import com.simprints.id.data.db.person.local.models.toRealmPerson
import com.simprints.id.data.db.person.domain.Fingerprint
import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class DbPersonTest {

    @Test
    fun buildRlPersonWithoutFingerprint() {
        val domainPerson = Person("guid", "projectId", "userId", "moduleId",
            emptyList())

        val dbPerson = domainPerson.toRealmPerson()

        Assert.assertEquals(dbPerson.patientId, "guid")
        Assert.assertEquals(dbPerson.userId, "userId")
        Assert.assertNull(dbPerson.createdAt)
        Assert.assertNull(dbPerson.updatedAt)
        Assert.assertEquals(dbPerson.moduleId, "moduleId")
        Assert.assertEquals(dbPerson.projectId, "projectId")
        Assert.assertTrue(dbPerson.fingerprints.isEmpty())
        Assert.assertTrue(dbPerson.toSync)
    }

    @Test
    fun buildRlPersonWithFingerprint() {
        val domainPerson = Person("guid", "projectId", "userId", "moduleId",
            listOf(Fingerprint(FingerIdentifier.LEFT_3RD_FINGER, FingerprintGeneratorUtils.generateRandomFingerprint().templateBytes)))

        val dbPerson = domainPerson.toRealmPerson()

        Assert.assertEquals(dbPerson.patientId, "guid")
        Assert.assertEquals(dbPerson.userId, "userId")
        Assert.assertNull(dbPerson.createdAt)
        Assert.assertNull(dbPerson.updatedAt)
        Assert.assertEquals(dbPerson.moduleId, "moduleId")
        Assert.assertEquals(dbPerson.projectId, "projectId")
        Assert.assertEquals(dbPerson.fingerprints.first()!!.fingerId, FingerIdentifier.LEFT_3RD_FINGER.ordinal)
        Assert.assertTrue(dbPerson.toSync)
    }

    @Test
    fun buildRlPersonFromACompleteFbPerson() {
        val domainPerson = Person("guid", "projectId", "userId", "moduleId",
            emptyList(), Date(0), Date(1), false)

        val dbPerson = domainPerson.toRealmPerson()

        Assert.assertEquals(dbPerson.patientId, "guid")
        Assert.assertEquals(dbPerson.userId, "userId")
        Assert.assertEquals(dbPerson.createdAt, Date(0))
        Assert.assertEquals(dbPerson.updatedAt, Date(1))
        Assert.assertEquals(dbPerson.moduleId, "moduleId")
        Assert.assertEquals(dbPerson.projectId, "projectId")
        Assert.assertFalse(dbPerson.toSync)
    }
}
