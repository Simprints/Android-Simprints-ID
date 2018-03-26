package com.simprints.id.data.db.remote.models

import com.google.gson.JsonObject
import com.simprints.id.BuildConfig
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.json.JsonHelper
import com.simprints.id.tools.utils.PeopleGeneratorUtils
import com.simprints.libcommon.Fingerprint
import com.simprints.libcommon.Person
import com.simprints.libsimprints.FingerIdentifier
import junit.framework.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class fb_PersonTest {

    @Test
    fun buildFbPersonFromRlPerson() {
        val rlPerson = PeopleGeneratorUtils.getRandomPerson().also {
            it.fingerprints.clear()
            it.fingerprints.add(PeopleGeneratorUtils.getRandomFingerprint().also { it.fingerId = FingerIdentifier.RIGHT_THUMB.ordinal })
            it.fingerprints.add(PeopleGeneratorUtils.getRandomFingerprint().also { it.fingerId = FingerIdentifier.LEFT_THUMB.ordinal })
        }

        val fbPerson = fb_Person(rlPerson)

        Assert.assertEquals(fbPerson.patientId, rlPerson.patientId)
        Assert.assertEquals(fbPerson.userId, rlPerson.userId)
        Assert.assertEquals(fbPerson.createdAt, rlPerson.createdAt)
        Assert.assertEquals(fbPerson.updatedAt, rlPerson.updatedAt)
        Assert.assertEquals(fbPerson.moduleId, rlPerson.moduleId)
        Assert.assertEquals(fbPerson.projectId, rlPerson.projectId)
        Assert.assertEquals(fbPerson.fingerprints.values.flatten().size, rlPerson.fingerprints.size)
        Assert.assertEquals(fbPerson.fingerprintsAsList.size, rlPerson.fingerprints.size)
    }

    @Test
    fun buildFbPersonFromPerson() {
        val person = Person("giud",
            ArrayList<Fingerprint>().also {
                it.add(Fingerprint.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB))
                it.add(Fingerprint.generateRandomFingerprint(FingerIdentifier.RIGHT_THUMB))
            })

        val fbPerson = fb_Person(person, "projectId", "userId", "moduleId")

        Assert.assertEquals(fbPerson.patientId, person.guid)
        Assert.assertEquals(fbPerson.userId, "userId")
        Assert.assertNull(fbPerson.createdAt)
        Assert.assertNull(fbPerson.updatedAt)
        Assert.assertEquals(fbPerson.moduleId, "moduleId")
        Assert.assertEquals(fbPerson.projectId, "projectId")
        Assert.assertEquals(fbPerson.fingerprints.values.flatten().size, person.fingerprints.size)
        Assert.assertEquals(fbPerson.fingerprintsAsList.size, person.fingerprints.size)
    }

    @Test
    fun serialiseFbPerson_skipUnwantedFields() {
        val fbPerson = fb_Person(PeopleGeneratorUtils.getRandomPerson())

        val jsonString = JsonHelper.toJson(fbPerson)
        val json = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        print(jsonString)
        Assert.assertTrue(json.has("id"))
        Assert.assertTrue(json.has("projectId"))
        Assert.assertTrue(json.has("userId"))
        Assert.assertTrue(json.has("moduleId"))
        Assert.assertTrue(json.has("createdAt"))
        Assert.assertTrue(json.has("updatedAt"))
        Assert.assertTrue(json.has("fingerprints"))

        Assert.assertEquals(json.keySet().size, 7)
    }
}
