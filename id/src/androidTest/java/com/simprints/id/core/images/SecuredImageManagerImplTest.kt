package com.simprints.id.core.images

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.simprints.core.images.SecuredImageRef
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.*
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
@SmallTest
class SecuredImageManagerImplTest {

    companion object {
        private const val FILENAME = "test"
        private const val SIZE_IMAGE = 500 * 1024 //500kB
        private const val IMAGES_FOLDER = "images"
    }

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val imagesFolder = "${app.filesDir}/$IMAGES_FOLDER"
    private val securedImageManager = SecuredImageManagerImpl(app)

    @Test
    fun givenAByteArray_storeIt_shouldCreateAFile() {
        val byteArray = Random.Default.nextBytes(SIZE_IMAGE)
        val securedImageRef = securedImageManager.storeImage(byteArray, FILENAME)
        require(securedImageRef != null)

        val file = File(securedImageRef.path)

        assertThat(file.absolutePath).isEqualTo("$imagesFolder/$FILENAME")
        assertThat(securedImageRef.path).contains(FILENAME)
        assertThat(file.readBytes()).isNotEqualTo(byteArray)
    }

    @Test
    fun givenAnEncryptedFile_decryptIt_shouldReturnTheRightContent() {
        val byteArray = Random.Default.nextBytes(SIZE_IMAGE)
        val securedImageRef = securedImageManager.storeImage(byteArray, FILENAME)
        require(securedImageRef != null)

        val encryptedInputStream = securedImageManager.readImage(securedImageRef)
        assertThat(encryptedInputStream?.readBytes()).isEqualTo(byteArray)
    }

    @Test
    fun encryptThrowsAnException_shouldBeHandled() {
        val securedImageRef = securedImageManager.storeImage(emptyArray<Byte>().toByteArray(), "")
        assertThat(securedImageRef).isNull()
    }

    @Test
    fun shouldListImageFiles() {
        val expectedFileCount = createImageFiles(10).size
        val actualFileCount = securedImageManager.listImages().size

        assertThat(actualFileCount).isEqualTo(expectedFileCount)
    }

    @Test
    fun shouldDeleteImageFiles() {
        val files = createImageFiles(3)
        val fileToDelete = files.first()
        securedImageManager.deleteImage(fileToDelete)
        val remainingFiles = securedImageManager.listImages()

        assertThat(remainingFiles.none { it.path == fileToDelete.path }).isTrue()
        assertThat(remainingFiles.size).isEqualTo(2)
    }

    @Test
    fun shouldHandleDeletionOfNonExistentImageFiles() {
        val file = SecuredImageRef("non/existent/path")

        assertThat(securedImageManager.deleteImage(file)).isFalse()
    }

    private fun createImageFiles(count: Int): List<SecuredImageRef> {
        val createdFiles = arrayListOf<SecuredImageRef>()

        for (i in 0 until count) {
            val byteArray = Random.nextBytes(SIZE_IMAGE)
            securedImageManager.storeImage(byteArray, UUID.randomUUID().toString())?.let(createdFiles::add)
        }

        return createdFiles
    }

}
