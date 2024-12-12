package com.simprints.fingerprint.infra.simafiswrapper.models

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.validTemplate
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.validTemplateWithLowQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimAfisPersonTest {
    @Test
    fun testConstructorWithFingerprints() {
        val guid = "1234567890"
        val fingerprints = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val person = SimAfisPerson(guid, fingerprints)

        assertEquals(guid, person.guid)
        assertEquals(2, person.fingerprints.size)
        assertTrue(person.fingerprints.containsKey(SimAfisFingerIdentifier.LEFT_THUMB))
        assertTrue(person.fingerprints.containsKey(SimAfisFingerIdentifier.LEFT_INDEX_FINGER))
    }

    @Test
    fun testHasBetterOrSameThan() {
        val guid = "1234567890"
        val sameFingerprint = SimAfisFingerprint(
            SimAfisFingerIdentifier.LEFT_INDEX_FINGER,
            validTemplateWithLowQuality,
        )
        val betterFingerprint =
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate)
        val fingerprints = listOf(sameFingerprint, betterFingerprint)
        val person = SimAfisPerson(guid, fingerprints)
        assertTrue(person.fingerprints[SimAfisFingerIdentifier.LEFT_INDEX_FINGER] == betterFingerprint)
    }

    @Test
    fun testEquals() {
        val guid = "1234567890"
        val fingerprints1 = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val fingerprints2 = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val person1 = SimAfisPerson(guid, fingerprints1)
        val person2 = SimAfisPerson(guid, fingerprints2)

        assertTrue(person1 == person2)
    }

    @Test
    fun testToString() {
        val guid = "1234567890"
        val fingerprints = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val person = SimAfisPerson(guid, fingerprints)

        val expectedString = "Person $guid, Fingerprints:\n" +
            "${fingerprints[0]}\n" +
            "${fingerprints[1]}\n\n"
        assertEquals(expectedString, person.toString())
    }

    @Test
    fun testReadAndWriteToParcel() {
        val guid = "1234567890"
        val fingerprints = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val person = SimAfisPerson(guid, fingerprints)

        val parcel = Parcel.obtain()
        person.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val personFromParcel = SimAfisPerson.CREATOR.createFromParcel(parcel)

        assertEquals(person, personFromParcel)
    }

    @Test
    fun testHashCode() {
        val guid = "1234567890"
        val fingerprints = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val person = SimAfisPerson(guid, fingerprints)
        val person2 = SimAfisPerson(guid, fingerprints)

        assertEquals(person.hashCode(), person2.hashCode())
    }

    @Test
    fun testGetFingerprints() {
        val guid = "1234567890"
        val fingerprints = listOf(
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, validTemplate),
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_INDEX_FINGER, validTemplate),
        )
        val person = SimAfisPerson(guid, fingerprints)

        assertEquals(fingerprints.size, person.fingerprints.values.size)
    }
}
