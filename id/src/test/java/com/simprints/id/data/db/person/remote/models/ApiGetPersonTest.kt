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
import org.junit.Assert.*
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

        assertEquals(apiPerson.id, dbPerson.patientId)
        assertEquals(apiPerson.userId, dbPerson.userId)
        assertEquals(apiPerson.createdAt, dbPerson.createdAt)
        assertEquals(apiPerson.updatedAt, dbPerson.updatedAt)
        assertEquals(apiPerson.moduleId, dbPerson.moduleId)
        assertEquals(apiPerson.projectId, dbPerson.projectId)
        assertEquals(apiPerson.fingerprints?.size, dbPerson.fingerprintSamples.size)
    }

    @Test
    fun buildApiPersonFromDomainPerson() {
        val domainPerson = Person("guid", "projectId", "userId", "moduleId",
            fingerprintSamples = listOf(
                FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB),
                FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.RIGHT_THUMB)
            ))

        val apiPerson = domainPerson.fromDomainToGetApi()

        assertEquals(apiPerson.id, domainPerson.patientId)
        assertEquals(apiPerson.userId, "userId")
        assertNull(apiPerson.createdAt)
        assertNull(apiPerson.updatedAt)
        assertEquals(apiPerson.moduleId, "moduleId")
        assertEquals(apiPerson.projectId, "projectId")
        assertEquals(apiPerson.fingerprints?.size, domainPerson.fingerprintSamples.size)
    }

    @Test
    fun deserialiseApiPerson() {
        val apiPerson = JsonHelper.gson.fromJson(apiPersonJson, ApiGetPerson::class.java)
        assertEquals(apiPerson.id, "aeed3784-a399-445a-9dcd-0a373184709c")
        assertEquals(apiPerson.projectId, "test10MProject")
        assertEquals(apiPerson.moduleId, "module2")
        assertEquals(apiPerson.userId, "user2")
        assertEquals(apiPerson.createdAt!!.time.toString(), "1520621879620")
        assertEquals(apiPerson.updatedAt!!.time.toString(), "1520621879620")
        assertEquals(apiPerson.fingerprints?.size, 2)

        val fingerprints = apiPerson.fingerprints
        assertNotNull(fingerprints)
        assertEquals(fingerprints?.first()?.finger, FingerIdentifier.LEFT_THUMB)
        assertEquals(fingerprints?.first()?.quality, 52)
        assertEquals(fingerprints?.first()?.template, "Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA")
    }

    @Test
    fun serialiseApiPerson_skipUnwantedFields() {
        val apiPerson = PeopleGeneratorUtils.getRandomPerson().fromDomainToGetApi()
        val jsonString = JsonHelper.toJson(apiPerson)
        val personJson = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        assertTrue(personJson.has("id"))
        assertTrue(personJson.has("projectId"))
        assertTrue(personJson.has("userId"))
        assertTrue(personJson.has("moduleId"))
        assertTrue(personJson.get("createdAt").asJsonPrimitive.isNumber)
        assertTrue(personJson.get("updatedAt").asJsonPrimitive.isNumber)
        assertTrue(personJson.has("fingerprints"))
        assert(personJson.has("deleted"))

        val fingerprintJson = personJson.get("fingerprints").asJsonArray.first().asJsonObject
        assertTrue(fingerprintJson.has("finger"))
        assertTrue(fingerprintJson.has("quality"))
        assertTrue(fingerprintJson.has("template"))

        assertEquals(personJson.keySet().size, 8)
        assertEquals(fingerprintJson.keySet().size, 3)
    }
}
