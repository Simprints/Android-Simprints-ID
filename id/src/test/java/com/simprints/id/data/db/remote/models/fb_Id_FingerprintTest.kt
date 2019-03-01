package com.simprints.id.data.db.remote.models

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.JsonObject
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.testtools.TestApplication
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.domain.fingerprint.IdFingerprint
import com.simprints.libsimprints.FingerIdentifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])

class fb_Id_FingerprintTest {
    @Test
    fun serialiseFbFingerprint_skipUnwantedFields() {
        val fingerprint = ApiFingerprint(IdFingerprint.generateRandomFingerprint(FingerIdentifier.LEFT_THUMB))
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
