package com.simprints.id.tools

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.io.BaseEncoding
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.id.testtools.TestApplication
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class Base64Test {

    @Test
    fun testAndroidBase64ReturnsSameValueOfGuava() {

        //Alternative to CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this)
        val decodeWithBase64 = BaseEncoding.base64().decode("test")
        val decodeWithAndroid = android.util.Base64.decode("test", Base64.NO_WRAP)

        val encodeWithBase64 = BaseEncoding.base64().encode("test".toByteArray())
        val encodeWithAndroid = android.util.Base64.encodeToString("test".toByteArray(), Base64.NO_WRAP)

        Assert.assertTrue(Arrays.equals(decodeWithBase64, decodeWithAndroid))
        Assert.assertEquals(encodeWithBase64, encodeWithAndroid)
    }
}
