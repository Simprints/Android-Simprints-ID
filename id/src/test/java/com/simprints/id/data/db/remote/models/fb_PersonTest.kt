package com.simprints.id.data.db.remote.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.data.db.local.realm.models.toRealmFingerprint
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.shared.PeopleGeneratorUtils
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.json.JsonHelper
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
class fb_PersonTest {

    val fbPersonJson = "{\"id\":\"aeed3784-a399-445a-9dcd-0a373184709c\",\"projectId\":\"test10MProject\",\"moduleId\":\"module2\",\"userId\":\"user2\",\"createdAt\":1520621879620,\"updatedAt\":1520621879620,\"fingerprints\":{\"LEFT_THUMB\":[{\"quality\":52,\"template\":\"Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA\"}],\"LEFT_INDEX_FINGER\":[{\"quality\":60,\"template\":\"Rk1SACAyMAAAAAGkAAABLAGQAMUAxQEAABBPQUAcACxpAEA3ADd4AEAsADv4AIAfAD5uAEA0AEh7AECPAFFzAIBkAF14AEA/AF54AEAZAGmHAEA0AG/7AEANAHkNAEBRAHn1AEAVAHqRAEBgAH11AIAbAH4GAEAjAISKAEAGAI8UAIBUAJF7AEC7AJnqAECWALDkAIAzALGQAEAVALMRAIByALZyAIAiALyeAECLAMnhAIBEAMuMAICDANBiAEA2ANMVAICqANdiAEAwANmuAEBuAOFxAIA8AOOhAIApAOgpAECqAO1nAEAZAO+zAEBFAPGzAEBdAPSbAEBjAPb0AIB6APZpAEDNAP5oAIB0AP9sAEBfAQH0AEBfAQPRAEBoAQroAIAhAQq7AEDYARd1AEDGARh4AIBfARhNAEB3ARnqAEBYARnZAIBuAR3kAEAYASO7AEDFASyGAICVAS5vAIBqATReAEA4ATjdAEAtATzaAEDGAUOGAEBGAUldAEDEAVCNAECjAVN9AEA4AVpiAEBPAWRoAECjAW6DAIBwAXl1AAAA\"}]}}"
    @Test
    fun buildFbPersonFromRlPerson() {
        val rlPerson = PeopleGeneratorUtils.getRandomPerson("patientId").toRealmPerson().apply {
            fingerprints.clear()
            fingerprints.add(PeopleGeneratorUtils.getRandomFingerprint().toRealmFingerprint().also { it.fingerId = FingerIdentifier.RIGHT_THUMB.ordinal })
            fingerprints.add(PeopleGeneratorUtils.getRandomFingerprint().toRealmFingerprint().also { it.fingerId = FingerIdentifier.LEFT_THUMB.ordinal })
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
    fun deserialiseFbPerson() {
        val person = JsonHelper.gson.fromJson(fbPersonJson, fb_Person::class.java)
        Assert.assertEquals(person.patientId, "aeed3784-a399-445a-9dcd-0a373184709c")
        Assert.assertEquals(person.projectId, "test10MProject")
        Assert.assertEquals(person.moduleId, "module2")
        Assert.assertEquals(person.userId, "user2")
        Assert.assertEquals(person.createdAt!!.time.toString(), "1520621879620")
        Assert.assertEquals(person.updatedAt!!.time.toString(), "1520621879620")
        Assert.assertEquals(person.fingerprints.values.size, 2)

        val fingerprints = person.fingerprints[FingerIdentifier.LEFT_THUMB]
        Assert.assertNotNull(fingerprints)
        Assert.assertEquals(fingerprints?.first()?.fingerId, FingerIdentifier.LEFT_THUMB)
        Assert.assertEquals(fingerprints?.first()?.quality, 52)
        Assert.assertEquals(fingerprints?.first()?.template, "Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA")
    }

    @Test
    fun serialiseFbPerson_skipUnwantedFields() {
        val fbPerson = PeopleGeneratorUtils.getRandomPerson().toFirebasePerson()
        val jsonString = JsonHelper.toJson(fbPerson)
        val personJson = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        Assert.assertTrue(personJson.has("id"))
        Assert.assertTrue(personJson.has("projectId"))
        Assert.assertTrue(personJson.has("userId"))
        Assert.assertTrue(personJson.has("moduleId"))
        Assert.assertTrue(personJson.get("createdAt").asJsonPrimitive.isNumber)
        Assert.assertTrue(personJson.get("updatedAt").asJsonPrimitive.isNumber)
        Assert.assertTrue(personJson.has("fingerprints"))
        val fingerprintId = fbPerson.fingerprintsAsList.first().fingerId.name
        val fingerprintJson = personJson.get("fingerprints").asJsonObject.get(fingerprintId).asJsonArray.first().asJsonObject
        Assert.assertTrue(fingerprintJson.has("quality"))
        Assert.assertTrue(fingerprintJson.has("template"))

        Assert.assertEquals(personJson.keySet().size, 7)
        Assert.assertEquals(fingerprintJson.keySet().size, 2)
    }
}
