package com.simprints.infra.images.metadata

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.images.metadata.database.DbImageMetadata
import com.simprints.infra.images.metadata.database.ImageMetadataDao
import com.simprints.infra.images.model.Path
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ImageMetadataStoreTest {
    @MockK
    private lateinit var metadataDao: ImageMetadataDao

    private lateinit var metadataStore: ImageMetadataStore

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        metadataStore = ImageMetadataStore(metadataDao)
    }

    @Test
    fun `store metadata`() = runTest {
        coJustRun { metadataDao.save(any()) }

        metadataStore.storeMetadata(
            Path("path/imageKey"),
            mapOf("key1" to "value1", "key2" to "value2"),
        )

        coVerify {
            metadataDao.save(
                listOf(
                    DbImageMetadata("imageKey", "key1", "value1"),
                    DbImageMetadata("imageKey", "key2", "value2"),
                ),
            )
        }
    }

    @Test
    fun `store empty metadata`() = runTest {
        metadataStore.storeMetadata(Path("path/imageKey"), emptyMap())

        coVerify(exactly = 0) { metadataDao.save(any()) }
    }

    @Test
    fun `getting metadata`() = runTest {
        coEvery { metadataDao.get("imageKey") } returns listOf(
            DbImageMetadata("imageKey", "key1", "value1"),
            DbImageMetadata("imageKey", "key2", "value2"),
        )

        assertThat(metadataStore.getMetadata(Path("imageKey")))
            .containsExactly("key1", "value1", "key2", "value2")
    }

    @Test
    fun `getting empty metadata`() = runTest {
        coEvery { metadataDao.get("imageKey") } returns emptyList()

        assertThat(metadataStore.getMetadata(Path("imageKey"))).isEmpty()
    }

    @Test
    fun `delete metadata`() = runTest {
        coJustRun { metadataDao.delete(any()) }

        metadataStore.deleteMetadata(Path("imageKey"))
        coVerify { metadataDao.delete("imageKey") }
    }

    @Test
    fun `delete all metadata`() = runTest {
        coJustRun { metadataDao.deleteAll() }

        metadataStore.deleteAllMetadata()
        coVerify { metadataDao.deleteAll() }
    }
}
