package com.simprints.id.data.db.image.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.image.local.ImageLocalDataSource
import com.simprints.id.data.db.image.remote.ImageRemoteDataSource
import com.simprints.id.data.db.image.remote.UploadResult
import com.simprints.testtools.common.syntax.whenever
import com.simprints.testtools.common.syntax.wheneverOnSuspend
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class ImageRepositoryImplTest {

    @Mock
    lateinit var localDataSource: ImageLocalDataSource

    @Mock
    lateinit var remoteDataSource: ImageRemoteDataSource

    private lateinit var repository: ImageRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        repository = ImageRepositoryImpl(localDataSource, remoteDataSource)
        initialiseMocks()
    }

    @Test
    fun shouldDeleteExistingImages() {
        val deletedSuccessfully = runBlocking {
            repository.deleteImage(SecuredImageRef(VALID_PATH))
        }

        assertThat(deletedSuccessfully).isTrue()
    }

    @Test
    fun shouldNotDeleteNonExistingImages() {
        val deletedSuccessfully = runBlocking {
            repository.deleteImage(SecuredImageRef(INVALID_PATH))
        }

        assertThat(deletedSuccessfully).isFalse()
    }

    @Test
    fun shouldUploadExistingImages() {
        configureLocalImageFiles()
        val uploads = runBlocking {
            repository.uploadImages()
        }

        val allUploadsSuccessful = uploads.all { it.isSuccessful() }
        assertThat(allUploadsSuccessful).isTrue()
    }

    @Test
    fun shouldNotUploadNonExistingImages() {
        configureLocalImageFiles(numberOfInvalidFiles = 2)
        val uploads = runBlocking {
            repository.uploadImages()
        }

        val allUploadsSuccessful = uploads.all { it.isSuccessful() }
        val failedUploadsCount = uploads.count { !it.isSuccessful() }

        assertThat(allUploadsSuccessful).isFalse()
        assertThat(failedUploadsCount).isEqualTo(2)
    }

    private fun initialiseMocks() {
        whenever(localDataSource) {
            deleteImage(SecuredImageRef(VALID_PATH))
        } thenReturn true

        whenever(localDataSource) {
            deleteImage(SecuredImageRef(INVALID_PATH))
        } thenReturn false

        wheneverOnSuspend(remoteDataSource) {
            uploadImage(SecuredImageRef(VALID_PATH))
        } thenOnBlockingReturn UploadResult(SecuredImageRef(VALID_PATH), UploadResult.Status.SUCCESSFUL)

        wheneverOnSuspend(remoteDataSource) {
            uploadImage(SecuredImageRef(INVALID_PATH))
        } thenOnBlockingReturn UploadResult(SecuredImageRef(INVALID_PATH), UploadResult.Status.FAILED)
    }

    private fun configureLocalImageFiles(numberOfInvalidFiles: Int = 0) {
        val files = mutableListOf(
            SecuredImageRef(VALID_PATH),
            SecuredImageRef(VALID_PATH),
            SecuredImageRef(VALID_PATH)
        ).apply {
            repeat(numberOfInvalidFiles) {
                add(SecuredImageRef(INVALID_PATH))
            }
        }

        whenever(localDataSource.listImages()) thenReturn files
    }

    companion object {
        private const val VALID_PATH = "valid/path"
        private const val INVALID_PATH = "invalid/path"
    }

}
