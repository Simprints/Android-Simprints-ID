package com.simprints.id.data.db.image.repository

import com.google.common.truth.Truth.assertThat
import com.simprints.core.images.SecuredImageRef
import com.simprints.id.data.db.image.local.ImageLocalDataSource
import com.simprints.id.data.db.image.remote.ImageRemoteDataSource
import com.simprints.id.data.db.image.remote.UploadResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ImageRepositoryImplTest {

    @MockK lateinit var localDataSource: ImageLocalDataSource
    @MockK lateinit var remoteDataSource: ImageRemoteDataSource

    private lateinit var repository: ImageRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = ImageRepositoryImpl(localDataSource, remoteDataSource)
        initialiseMocks()
    }

    @Test
    fun withEmptyList_shouldConsiderUploadOperationSuccessful() = runBlockingTest {
        every { localDataSource.listImages() } returns emptyList()

        val successful = repository.uploadStoredImagesAndDelete()

        assertThat(successful).isTrue()
    }

    @Test
    fun withAllFilesValid_shouldUploadAndDeleteSuccessfully() = runBlockingTest {
        configureLocalImageFiles(includeInvalidFile = false)

        val successful = repository.uploadStoredImagesAndDelete()

        assertThat(successful).isTrue()
    }

    @Test
    fun withNotAllFilesValid_shouldNotConsiderUploadOperationSuccessful() = runBlockingTest {
        configureLocalImageFiles(includeInvalidFile = true)

        val successful = repository.uploadStoredImagesAndDelete()

        assertThat(successful).isFalse()
    }

    private fun initialiseMocks() {
        every {
            localDataSource.deleteImage(SecuredImageRef(VALID_PATH))
        } returns true

        every {
            localDataSource.deleteImage(SecuredImageRef(INVALID_PATH))
        } returns false

        coEvery {
            remoteDataSource.uploadImage(SecuredImageRef(VALID_PATH))
        } returns UploadResult(SecuredImageRef(VALID_PATH), UploadResult.Status.SUCCESSFUL)

        coEvery {
            remoteDataSource.uploadImage(SecuredImageRef(INVALID_PATH))
        } returns UploadResult(SecuredImageRef(INVALID_PATH), UploadResult.Status.FAILED)
    }

    private fun configureLocalImageFiles(includeInvalidFile: Boolean) {
        val files = mutableListOf(
            SecuredImageRef(VALID_PATH),
            SecuredImageRef(VALID_PATH),
            SecuredImageRef(VALID_PATH)
        ).apply {
            if (includeInvalidFile)
                add(SecuredImageRef(INVALID_PATH))
        }

        every { localDataSource.listImages() } returns files
    }

    companion object {
        private const val VALID_PATH = "valid/path"
        private const val INVALID_PATH = "invalid/path"
    }

}
