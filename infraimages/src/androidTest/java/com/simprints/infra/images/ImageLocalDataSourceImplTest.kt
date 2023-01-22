package com.simprints.infra.images

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.images.local.ImageLocalDataSourceImpl
import com.simprints.infra.images.model.Path
import com.simprints.infra.images.model.SecuredImageRef
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.random.Random

class ImageLocalDataSourceImplTest {

    companion object {
        private const val FILE_NAME = "test.png"
        private const val SIZE_IMAGE = 100 * 1024 //100kB
        private const val IMAGES_FOLDER = "images"
    }

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val imagesFolder = "${app.filesDir}/$IMAGES_FOLDER"
    private val path = Path("test/$FILE_NAME")
    private val imageLocalDataSource = ImageLocalDataSourceImpl(app, mockk())

    @Before
    fun setUp() {
        File(imagesFolder).deleteRecursively()
    }

    @Test
    fun givenAByteArray_storeIt_shouldReturnASecuredImageRef() {
        val byteArray = Random.Default.nextBytes(SIZE_IMAGE)
        val securedImageRef = imageLocalDataSource.encryptAndStoreImage(byteArray, path)
        require(securedImageRef != null)

        assertThat(securedImageRef.relativePath.compose()).contains(FILE_NAME)
    }

    @Test
    fun givenAnEncryptedFile_decryptIt_shouldReturnTheRightContent() {
        val byteArray = Random.Default.nextBytes(SIZE_IMAGE)
        val securedImageRef = imageLocalDataSource.encryptAndStoreImage(byteArray, path)
        require(securedImageRef != null)

        val encryptedInputStream = imageLocalDataSource.decryptImage(securedImageRef)
        assertThat(encryptedInputStream?.readBytes()).isEqualTo(byteArray)
    }

    @Test
    fun encryptThrowsAnException_shouldBeHandled() {
        val securedImageRef = imageLocalDataSource.encryptAndStoreImage(
            emptyArray<Byte>().toByteArray(),
            Path("")
        )
        assertThat(securedImageRef).isNull()
    }

    @Test
    fun shouldListImageFiles() {
        val expectedFileCount = createImageFiles(10).size
        val actualFileCount = imageLocalDataSource.listImages().size

        assertThat(actualFileCount).isEqualTo(expectedFileCount)
    }

    @Test
    fun shouldListImageFilesStoredAtDifferentSubDirs() {
        val bytes = Random.nextBytes(SIZE_IMAGE)
        with(imageLocalDataSource) {
            encryptAndStoreImage(bytes, Path("dir1/$FILE_NAME"))
            encryptAndStoreImage(bytes, Path("dir2/$FILE_NAME"))
        }

        val images = imageLocalDataSource.listImages()
        val actualFileCount = images.size

        assertThat(actualFileCount).isEqualTo(2)
    }

    @Test
    fun shouldDeleteImageFiles() {
        val files = createImageFiles(3)

        val fileToDelete = files.first()
        imageLocalDataSource.deleteImage(fileToDelete)
        val remainingFiles = imageLocalDataSource.listImages()

        assertThat(remainingFiles.none { it.relativePath == fileToDelete.relativePath }).isTrue()
        assertThat(remainingFiles.size).isEqualTo(2)
    }

    @Test
    fun shouldHandleDeletionOfNonExistingImageFiles() {
        val file = SecuredImageRef(Path("non/existing/path/file.txt"))

        assertThat(imageLocalDataSource.deleteImage(file)).isFalse()
    }

    private fun createImageFiles(count: Int): List<SecuredImageRef> {
        val createdFiles = arrayListOf<SecuredImageRef>()

        for (i in 0 until count) {
            val byteArray = Random.nextBytes(SIZE_IMAGE)
            imageLocalDataSource.encryptAndStoreImage(
                byteArray,
                Path("test/${UUID.randomUUID()}")
            )?.let(createdFiles::add)
        }

        return createdFiles
    }

}
