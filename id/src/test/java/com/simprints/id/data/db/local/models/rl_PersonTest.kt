package com.simprints.id.data.db.local.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.testframework.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.data.db.local.realm.models.rl_Person
import com.simprints.id.data.db.remote.models.fb_Person
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.commontesttools.TestApplication
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
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
            Person("guid"),
            "projectId",
            "userId",
            "moduleId")

        val rlPerson = rl_Person(fb_Person)

        Assert.assertEquals(rlPerson.patientId, "guid")
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
            Person("giud", arrayListOf(Fingerprint(
                FingerIdentifier.LEFT_3RD_FINGER,
                PeopleGeneratorUtils.getRandomFingerprint().template!!))),

            "projectId",
            "userId",
            "moduleId")

        val rlPerson = rl_Person(fb_Person)

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

        val rlPerson = rl_Person(fb_Person)

        Assert.assertEquals(rlPerson.patientId, "giud")
        Assert.assertEquals(rlPerson.userId, "userId")
        Assert.assertEquals(rlPerson.createdAt, Date(1))
        Assert.assertEquals(rlPerson.updatedAt, Date(0))
        Assert.assertEquals(rlPerson.moduleId, "moduleId")
        Assert.assertEquals(rlPerson.projectId, "projectId")
        Assert.assertFalse(rlPerson.toSync)
    }
}
