package com.simprints.id.data.db.remote.models

import com.google.gson.JsonObject
import com.simprints.id.activities.ShadowAndroidXMultiDex
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.json.JsonHelper
import com.simprints.libcommon.Fingerprint
import com.simprints.libsimprints.FingerIdentifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])

class fb_FingerprintTest {
    @Test
    fun serialiseFbFingerprint_skipUnwantedFields() {
        val fingerprint = fb_Fingerprint(Fingerprint.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB))
        val jsonString = JsonHelper.toJson(fingerprint)
        val json = JsonHelper.gson.fromJson(jsonString, JsonObject::class.java)

        assertTrue(json.has("quality"))
        assertTrue(json.has("template"))
        assertEquals(json.keySet().size, 2)
    }

    @Test
    fun test() {

        val right = FingerIdentifier.RIGHT_THUMB
        assertEquals(right.toString(), right.name)
    }
}
