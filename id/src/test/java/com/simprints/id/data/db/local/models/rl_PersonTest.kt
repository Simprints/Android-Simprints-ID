package com.simprints.id.data.db.local.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.data.db.local.realm.models.DbPerson
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.testtools.TestApplication
import com.simprints.id.domain.fingerprint.IdFingerprint
import com.simprints.id.domain.fingerprint.Person
import com.simprints.libsimprints.FingerIdentifier
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class rl_PersonTest {

    @Test
    fun buildRlPersonWithoutFingerprint() {
        val fb_Person = fb_Person(
            Person("guidFound"),
            "projectId",
            "userId",
            "moduleId")

        val rlPerson = DbPerson(fb_Person)

        Assert.assertEquals(rlPerson.patientId, "guidFound")
        Assert.assertEquals(rlPerson.userId, "userId")
        Assert.assertNull(rlPerson.createdAt)
        Assert.assertNull(rlPerson.updatedAt)
        Assert.assertEquals(rlPerson.moduleId, "moduleId")
        Assert.assertEquals(rlPerson.projectId, "projectId")
        Assert.assertTrue(rlPerson.fingerprints.isEmpty())
        Assert.assertTrue(rlPerson.toSync)
    }

    @Test
    fun buildRlPersonWithFingerprint() {
        val fb_Person = fb_Person(
            Person("giud", arrayListOf(IdFingerprint(
                FingerIdentifier.LEFT_3RD_FINGER,
                PeopleGeneratorUtils.getRandomFingerprint().template!!))),

            "projectId",
            "userId",
            "moduleId")

        val rlPerson = DbPerson(fb_Person)

        Assert.assertEquals(rlPerson.patientId, "giud")
        Assert.assertEquals(rlPerson.userId, "userId")
        Assert.assertNull(rlPerson.createdAt)
        Assert.assertNull(rlPerson.updatedAt)
        Assert.assertEquals(rlPerson.moduleId, "moduleId")
        Assert.assertEquals(rlPerson.projectId, "projectId")
        Assert.assertEquals(rlPerson.fingerprints.first()!!.fingerId, FingerIdentifier.LEFT_3RD_FINGER.ordinal)
        Assert.assertTrue(rlPerson.toSync)
    }

    @Test
    fun buildRlPersonFromACompleteFbPerson() {
        val fb_Person = fb_Person(
            Person("giud"),
            "projectId",
            "userId",
            "moduleId")

        fb_Person.updatedAt = Date(0)
        fb_Person.createdAt = Date(1)

        val rlPerson = DbPerson(fb_Person)

        Assert.assertEquals(rlPerson.patientId, "giud")
        Assert.assertEquals(rlPerson.userId, "userId")
        Assert.assertEquals(rlPerson.createdAt, Date(1))
        Assert.assertEquals(rlPerson.updatedAt, Date(0))
        Assert.assertEquals(rlPerson.moduleId, "moduleId")
        Assert.assertEquals(rlPerson.projectId, "projectId")
        Assert.assertFalse(rlPerson.toSync)
    }
}
