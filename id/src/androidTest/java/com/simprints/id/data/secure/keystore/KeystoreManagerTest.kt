package com.simprints.id.data.secure.keystore

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.simprints.id.Application
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class KeystoreManagerTest {

    @Test
    fun encryptAndDecryptString_shouldProduceSameOriginalString() {

        val originalString = "test"
        val keystoreManager = KeystoreManagerImpl(ApplicationProvider.getApplicationContext<Application>())
        val encrypt = keystoreManager.encryptString(originalString)
        val decrypt = keystoreManager.decryptString(encrypt)
        assertEquals(originalString, decrypt)
    }
}
