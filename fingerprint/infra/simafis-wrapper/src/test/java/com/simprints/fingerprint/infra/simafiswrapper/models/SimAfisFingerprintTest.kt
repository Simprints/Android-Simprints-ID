package com.simprints.fingerprint.infra.simafiswrapper.models

import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.FORMAT_ID
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.NB_FINGERPRINTS
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.RECORD_LENGTH
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.VERSION
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.createValidTemplate
import com.simprints.fingerprint.infra.simafiswrapper.models.TemplateGenerator.validTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimAfisFingerprintTest {
    @Test(expected = Test.None::class)
    fun testCheckTemplateValidity_ValidTemplate() {
        val template = validTemplate
        SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        // No exception should be thrown
    }

    @Test
    fun testCheckTemplateValidity_InvalidFormatId() {
        val template = createValidTemplate()
        // Change the format identifier to an invalid value
        template[FORMAT_ID] = 123
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        }
        assertEquals("Invalid template: not an ISO template", exception.message)
    }

    @Test
    fun testCheckTemplateValidity_InvalidVersion() {
        val template = createValidTemplate()
        // Change the ISO version to an invalid value
        template[VERSION] = 45
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        }
        assertEquals("Invalid template: only ISO 2005 is supported", exception.message)
    }

    @Test
    fun testCheckTemplateValidity_InvalidLength() {
        val template = createValidTemplate()
        // Change the record length to an invalid value
        template[RECORD_LENGTH] = 100
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        }
        assertEquals("Invalid template: invalid length", exception.message)
    }

    @Test
    fun testCheckTemplateValidity_InvalidNumberOfFingers() {
        val template = createValidTemplate()
        // Change the number of fingerprints to an invalid value
        template[NB_FINGERPRINTS] = 2.toByte()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        }
        assertEquals(
            "Invalid template: only single fingerprint templates are supported",
            exception.message,
        )
    }

    @Test
    fun testQualityScore() {
        val template = validTemplate
        val fingerprint = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        assertEquals(100, fingerprint.qualityScore)
    }

    @Test
    fun testEquals_SameObject() {
        val template = validTemplate
        val fingerprint = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        assertTrue(fingerprint.equals(fingerprint))
    }

    @Test
    fun testEquals_NullObject() {
        val template = validTemplate
        val fingerprint = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        assertFalse(fingerprint.equals(null))
    }

    @Test
    fun testEquals_DifferentClass() {
        val template = validTemplate
        val fingerprint = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)
        assertFalse(fingerprint.equals("not a fingerprint"))
    }

    @Test
    fun testEquals_EqualFingerprints() {
        val template1 = validTemplate
        val fingerprint1 = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template1)

        val template2 = validTemplate
        val fingerprint2 = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template2)

        assertTrue(fingerprint1 == fingerprint2)
    }

    @Test
    fun testEquals_DifferentFingerId() {
        val template1 = validTemplate
        val fingerprint1 = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template1)

        val template2 = validTemplate
        val fingerprint2 = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_4TH_FINGER, template2)

        assertFalse(fingerprint1 == fingerprint2)
    }

    @Test
    fun testEquals_DifferentTemplate() {
        val template1 = validTemplate
        val fingerprint1 = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template1)

        val template2 = validTemplate
        // Change one byte in the template
        template2[28] = 123.toByte()
        val fingerprint2 = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template2)

        assertFalse(fingerprint1.equals(fingerprint2))
    }

    @Test
    fun testReadAndWriteInParcel() {
        val template = validTemplate
        val fingerprint = SimAfisFingerprint(SimAfisFingerIdentifier.LEFT_THUMB, template)

        val parcel = Parcel.obtain()
        fingerprint.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val fingerprintFromParcel = SimAfisFingerprint.CREATOR.createFromParcel(parcel)

        assertEquals(fingerprint, fingerprintFromParcel)
    }
}
