package com.simprints.id.data.secure.keystore

import android.support.test.InstrumentationRegistry
import android.support.test.filters.SmallTest
import android.support.test.runner.AndroidJUnit4
import junit.framework.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class KeystoreManagerTest {

    @Before
    fun setUp() {
    }

    @Test
    fun encryptAndDecryptString_shouldProduceSameOriginalString() {

        val originalString = "test"
        val keystoreManager = KeystoreManagerImpl(InstrumentationRegistry.getInstrumentation().targetContext)
        val encrypt = keystoreManager.encryptString(originalString)
        val decrypt = keystoreManager.decryptString(encrypt)
        Assert.assertEquals(originalString, decrypt)
    }
}
