package com.simprints.id.data.db.remote.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.commontesttools.PeopleGeneratorUtils
import com.simprints.id.testtools.TestApplication
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.FingerIdentifier
import com.simprints.id.commontesttools.FingerprintGeneratorUtils
import com.simprints.id.data.db.local.realm.models.toDomainPerson
import com.simprints.id.data.db.local.realm.models.toRealmPerson
import com.simprints.id.domain.Person
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class ApiPersonTest {

    private val apiPersonJson = "{\"id\":\"aeed3784-a399-445a-9dcd-0a373184709c\",\"projectId\":\"test10MProject\",\"moduleId\":\"module2\",\"userId\":\"user2\",\"createdAt\":1520621879620,\"updatedAt\":1520621879620,\"fingerprints\":[{\"quality\":52,\"template\":\"Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA\",\"finger\":\"LEFT_THUMB\"},{\"quality\":60,\"template\":\"Rk1SACAyMAAAAAGkAAABLAGQAMUAxQEAABBPQUAcACxpAEA3ADd4AEAsADv4AIAfAD5uAEA0AEh7AECPAFFzAIBkAF14AEA/AF54AEAZAGmHAEA0AG/7AEANAHkNAEBRAHn1AEAVAHqRAEBgAH11AIAbAH4GAEAjAISKAEAGAI8UAIBUAJF7AEC7AJnqAECWALDkAIAzALGQAEAVALMRAIByALZyAIAiALyeAECLAMnhAIBEAMuMAICDANBiAEA2ANMVAICqANdiAEAwANmuAEBuAOFxAIA8AOOhAIApAOgpAECqAO1nAEAZAO+zAEBFAPGzAEBdAPSbAEBjAPb0AIB6APZpAEDNAP5oAIB0AP9sAEBfAQH0AEBfAQPRAEBoAQroAIAhAQq7AEDYARd1AEDGARh4AIBfARhNAEB3ARnqAEBYARnZAIBuAR3kAEAYASO7AEDFASyGAICVAS5vAIBqATReAEA4ATjdAEAtATzaAEDGAUOGAEBGAUldAEDEAVCNAECjAVN9AEA4AVpiAEBPAWRoAECjAW6DAIBwAXl1AAAA\",\"finger\":\"LEFT_INDEX_FINGER\"}],\"faces\":[{\"template\":\"ECDAO/HAIBfAPVVAIBRAPZ4AECbAPvHAEC7AP5RAEBuAQFQAEB7AQJKAEBhAQRiAIBEAQeqAEA+ARPQAECMARZHAEB3ARxRAECrASpRAEBmAStiAECTATJUAEBiAUVsAEByAUloAECFAUldAIA0AUpoAECUAUxfAECRAWNzAEB7AWp1AECKAYN9AAAA\",\"yaw\":0,\"pitch\":0}]}"

    @Test
    fun buildApiPersonFromDbPerson() {
        val dbPerson = PeopleGeneratorUtils.getRandomPerson("patientId", idFingerprints = arrayOf(
            FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB),
            FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.RIGHT_THUMB)
        )).toRealmPerson()

        val apiPerson = dbPerson.toDomainPerson().toApiPerson()

        assertEquals(apiPerson.patientId, dbPerson.patientId)
        assertEquals(apiPerson.userId, dbPerson.userId)
        assertEquals(apiPerson.createdAt, dbPerson.createdAt)
        assertEquals(apiPerson.updatedAt, dbPerson.updatedAt)
        assertEquals(apiPerson.moduleId, dbPerson.moduleId)
        assertEquals(apiPerson.projectId, dbPerson.projectId)
        assertEquals(apiPerson.fingerprints.size, dbPerson.fingerprints.size)
    }

    @Test
    fun buildApiPersonFromDomainPerson() {
        val domainPerson = Person("guid", "projectId", "userId", "moduleId",
            listOf(
                FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB),
                FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.RIGHT_THUMB)
            ))

        val apiPerson = domainPerson.toApiPerson()

        assertEquals(apiPerson.patientId, domainPerson.patientId)
        assertEquals(apiPerson.userId, "userId")
        assertNull(apiPerson.createdAt)
        assertNull(apiPerson.updatedAt)
        assertEquals(apiPerson.moduleId, "moduleId")
        assertEquals(apiPerson.projectId, "projectId")
        assertEquals(apiPerson.fingerprints.size, domainPerson.fingerprints.size)
    }

    @Test
    fun deserialiseApiPerson() {
        val apiPerson = JsonHelper.gson.fromJson(apiPersonJson, ApiPerson::class.java)
        assertEquals(apiPerson.patientId, "aeed3784-a399-445a-9dcd-0a373184709c")
        assertEquals(apiPerson.projectId, "test10MProject")
        assertEquals(apiPerson.moduleId, "module2")
        assertEquals(apiPerson.userId, "user2")
        assertEquals(apiPerson.createdAt!!.time.toString(), "1520621879620")
        assertEquals(apiPerson.updatedAt!!.time.toString(), "1520621879620")
        assertEquals(apiPerson.fingerprints.size, 2)

        val fingerprints = apiPerson.fingerprints

        assertNotNull(fingerprints)
        assertEquals(fingerprints.first().finger, FingerIdentifier.LEFT_THUMB)
        assertEquals(fingerprints.first().quality, 52)
        assertEquals(fingerprints.first().template, "Rk1SACAyMAAAAADMAAABLAGQAMUAxQEAABA7HYBEAGUJAEBpAHaDAEBaAHcDAEBdAJCNAIAiAJARAIBDAJqUAIAoAKgRAIBFAKgUAEBWALOUAIA/AMsRAECQAMx7AEBMAOGgAECNAOeAAECPAQaxAECKAQaxAECRAQvDAEA5AQuxAECeAQ/aAEBEARO4AIAfARisAECOARzDAIC8AStfAEAhAS7DAICWATtaAICMAUFaAEBvAVdhAEBXAVxhAEA+AWFkAICLAWJ1AAAA")
    }

    @Test
    fun serialiseApiPerson_skipUnwantedFields() {
        val apiPerson = PeopleGeneratorUtils.getRandomPerson().toApiPerson()
        val jsonString = JsonHelper.toJson(apiPerson)
        val personJson = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        assertTrue(personJson.has("id"))
        assertTrue(personJson.has("projectId"))
        assertTrue(personJson.has("userId"))
        assertTrue(personJson.has("moduleId"))
        assertTrue(personJson.get("createdAt").asJsonPrimitive.isNumber)
        assertTrue(personJson.get("updatedAt").asJsonPrimitive.isNumber)
        assertTrue(personJson.has("fingerprints") or personJson.has("faces"))

        apiPerson.fingerprints.first().finger.name
        val fingerprintJson = personJson.get("fingerprints").asJsonArray.first().asJsonObject

        assertTrue(fingerprintJson.has("quality"))
        assertTrue(fingerprintJson.has("template"))
        assertTrue(fingerprintJson.has("finger"))

        assertEquals(personJson.keySet().size, 7)
        assertEquals(fingerprintJson.keySet().size, 3)
    }
}
