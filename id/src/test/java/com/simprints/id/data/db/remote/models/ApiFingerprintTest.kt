package com.simprints.id.data.db.remote.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.FingerIdentifier
import com.simprints.id.commontesttools.FingerprintGeneratorUtils
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])

class ApiFingerprintTest {
    @Test
    fun serialiseFbFingerprint_skipUnwantedFields() {
        val fingerprint = ApiFingerprint(FingerprintGeneratorUtils.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB))
        val jsonString = JsonHelper.toJson(fingerprint)
        val json = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        assertTrue(json.has("quality"))
        assertTrue(json.has("template"))
        assertTrue(json.has("finger"))
        assertEquals(json.keySet().size, 3)
    }
}
