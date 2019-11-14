package com.simprints.id.core.images

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@SmallTest
class SecuredImageManagerImplTest {

    companion object {
        private const val FILENAME = "test"
    }

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val securedImageManager = SecuredImageManagerImpl(app)

    @Test
    fun givenAByteArray_storeIt_shouldCreateAFile() {
        val byteArray = Random.Default.nextBytes(100)
        val securedImageRef = securedImageManager.storeImage(byteArray, FILENAME)

        val file = File(securedImageRef.path)

        assertThat(securedImageRef.path).contains(FILENAME)
        assertThat(file.readBytes()).isNotEqualTo(byteArray)
    }

    @Test
    fun givenAEncryptedFile_decryptIt_shouldReturnTheRightContent() {
        val byteArray = Random.Default.nextBytes(100)
        val securedImageRef = securedImageManager.storeImage(byteArray, FILENAME)
        val encryptedInputStream = securedImageManager.readImage(securedImageRef)

        assertThat(encryptedInputStream?.readBytes()).isEqualTo(byteArray)
    }
}
