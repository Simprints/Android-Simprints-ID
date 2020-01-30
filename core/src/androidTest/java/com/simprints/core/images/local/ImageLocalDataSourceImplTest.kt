package com.simprints.core.images.local

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.simprints.core.images.model.Path
import com.simprints.core.images.model.SecuredImageRef
import com.simprints.core.tools.utils.randomUUID
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.random.Random

@SmallTest
class ImageLocalDataSourceImplTest {

    companion object {
        private const val FILE_NAME = "test.png"
        private const val SIZE_IMAGE = 500 * 1024 //500kB
        private const val IMAGES_FOLDER = "images"
    }

    private val app = ApplicationProvider.getApplicationContext<Application>()
    private val imagesFolder = "${app.filesDir}/$IMAGES_FOLDER"
    private val path = Path("test/$FILE_NAME")
    private val imageLocalDataSource = ImageLocalDataSourceImpl(app)

    @Before
    fun setUp() {
        File(imagesFolder).deleteRecursively()
    }

    @Test
    fun givenAByteArray_storeIt_shouldCreateAFile() {
        val byteArray = Random.Default.nextBytes(SIZE_IMAGE)
        val securedImageRef = imageLocalDataSource.encryptAndStoreImage(byteArray, path)
        require(securedImageRef != null)

        val file = File(securedImageRef.path.compose())

        assertThat(file.absolutePath).isEqualTo("$imagesFolder/test/$FILE_NAME")
        assertThat(securedImageRef.path.compose()).contains(FILE_NAME)
        assertThat(file.readBytes()).isNotEqualTo(byteArray)
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

        assertThat(remainingFiles.none { it.path == fileToDelete.path }).isTrue()
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
                Path("test/${randomUUID()}")
            )?.let(createdFiles::add)
        }

        return createdFiles
    }

}
