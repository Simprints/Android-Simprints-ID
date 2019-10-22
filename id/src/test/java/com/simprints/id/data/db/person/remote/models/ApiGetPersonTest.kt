package com.simprints.id.data.db.person.remote.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.testtools.TestApplication
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.commontesttools.FingerprintGeneratorUtils
import com.simprints.id.data.db.person.local.models.fromDbToDomain
import com.simprints.id.data.db.person.local.models.fromDomainToDb
import com.simprints.id.data.db.person.domain.Person
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ApiGetPersonTest {

    private val apiPersonJson = "{\"id\":\"aeed3784-a399-445a-9dcd-0a373184709c\",\"projectId\":\"test10MProject\",\"moduleId\":\"module2\",\"userId\":\"user2\",\"createdAt\":1520621879620,\"updatedAt\":1520621879620,\"fingerprints\":[{\"quality\":52,\"template\":\"Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA\",\"finger\":\"LEFT_THUMB\"},{\"quality\":60,\"template\":\"Rk1SACAyMAAAAAGkAAABLAGQAMUAxQEAABBPQUAcACxpAEA3ADd4AEAsADv4AIAfAD5uAEA0AEh7AECPAFFzAIBkAF14AEA/AF54AEAZAGmHAEA0AG/7AEANAHkNAEBRAHn1AEAVAHqRAEBgAH11AIAbAH4GAEAjAISKAEAGAI8UAIBUAJF7AEC7AJnqAECWALDkAIAzALGQAEAVALMRAIByALZyAIAiALyeAECLAMnhAIBEAMuMAICDANBiAEA2ANMVAICqANdiAEAwANmuAEBuAOFxAIA8AOOhAIApAOgpAECqAO1nAEAZAO+zAEBFAPGzAEBdAPSbAEBjAPb0AIB6APZpAEDNAP5oAIB0AP9sAEBfAQH0AEBfAQPRAEBoAQroAIAhAQq7AEDYARd1AEDGARh4AIBfARhNAEB3ARnqAEBYARnZAIBuAR3kAEAYASO7AEDFASyGAICVAS5vAIBqATReAEA4ATjdAEAtATzaAEDGAUOGAEBGAUldAEDEAVCNAECjAVN9AEA4AVpiAEBPAWRoAECjAW6DAIBwAXl1AAAA\",\"finger\":\"LEFT_INDEX_FINGER\"}],\"faces\":[{\"template\":\"ECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\"}]}"

    @Test
    fun buildApiPersonFromDbPerson() {
        val dbPerson = PeopleGeneratorUtils.getRandomPerson("patientId", fingerprintSamples = arrayOf(
            FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB),
            FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.RIGHT_THUMB)
        )).fromDomainToDb()

        val apiPerson = dbPerson.fromDbToDomain().fromDomainToGetApi()

        Assert.assertEquals(apiPerson.id, dbPerson.patientId)
        Assert.assertEquals(apiPerson.userId, dbPerson.userId)
        Assert.assertEquals(apiPerson.createdAt, dbPerson.createdAt)
        Assert.assertEquals(apiPerson.updatedAt, dbPerson.updatedAt)
        Assert.assertEquals(apiPerson.moduleId, dbPerson.moduleId)
        Assert.assertEquals(apiPerson.projectId, dbPerson.projectId)
        Assert.assertEquals(apiPerson.fingerprints?.size, dbPerson.fingerprintSamples.size)
    }

    @Test
    fun buildApiPersonFromDomainPerson() {
        val domainPerson = Person("guid", "projectId", "userId", "moduleId",
            fingerprintSamples = listOf(
                FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB),
                FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.RIGHT_THUMB)
            ))

        val apiPerson = domainPerson.fromDomainToGetApi()

        Assert.assertEquals(apiPerson.id, domainPerson.patientId)
        Assert.assertEquals(apiPerson.userId, "userId")
        Assert.assertNull(apiPerson.createdAt)
        Assert.assertNull(apiPerson.updatedAt)
        Assert.assertEquals(apiPerson.moduleId, "moduleId")
        Assert.assertEquals(apiPerson.projectId, "projectId")
        Assert.assertEquals(apiPerson.fingerprints?.size, domainPerson.fingerprintSamples.size)
    }

    @Test
    fun deserialiseApiPerson() {
        val apiPerson = JsonHelper.gson.fromJson(apiPersonJson, ApiGetPerson::class.java)
        Assert.assertEquals(apiPerson.id, "aeed3784-a399-445a-9dcd-0a373184709c")
        Assert.assertEquals(apiPerson.projectId, "test10MProject")
        Assert.assertEquals(apiPerson.moduleId, "module2")
        Assert.assertEquals(apiPerson.userId, "user2")
        Assert.assertEquals(apiPerson.createdAt!!.time.toString(), "1520621879620")
        Assert.assertEquals(apiPerson.updatedAt!!.time.toString(), "1520621879620")
        Assert.assertEquals(apiPerson.fingerprints?.size, 2)

        val fingerprints = apiPerson.fingerprints
        Assert.assertNotNull(fingerprints)
        Assert.assertEquals(fingerprints?.first()?.finger, FingerIdentifier.LEFT_THUMB)
        Assert.assertEquals(fingerprints?.first()?.quality, 52)
        Assert.assertEquals(fingerprints?.first()?.template, "Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA")
    }

    @Test
    fun serialiseApiPerson_skipUnwantedFields() {
        val apiPerson = PeopleGeneratorUtils.getRandomPerson().fromDomainToGetApi()
        val jsonString = JsonHelper.toJson(apiPerson)
        val personJson = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        Assert.assertTrue(personJson.has("id"))
        Assert.assertTrue(personJson.has("projectId"))
        Assert.assertTrue(personJson.has("userId"))
        Assert.assertTrue(personJson.has("moduleId"))
        Assert.assertTrue(personJson.get("createdAt").asJsonPrimitive.isNumber)
        Assert.assertTrue(personJson.get("updatedAt").asJsonPrimitive.isNumber)
        Assert.assertTrue(personJson.has("fingerprints"))

        val fingerprintJson = personJson.get("fingerprints").asJsonArray.first().asJsonObject
        Assert.assertTrue(fingerprintJson.has("finger"))
        Assert.assertTrue(fingerprintJson.has("quality"))
        Assert.assertTrue(fingerprintJson.has("template"))

        Assert.assertEquals(personJson.keySet().size, 7)
        Assert.assertEquals(fingerprintJson.keySet().size, 3)
    }
}
