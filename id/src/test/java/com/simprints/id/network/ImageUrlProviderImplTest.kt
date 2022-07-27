package com.simprints.id.network

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.infra.login.domain.LoginInfoManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ImageUrlProviderImplTest {

    @MockK
    lateinit var mockProjectLocalDataSource: ProjectLocalDataSource

    @MockK
    lateinit var mockLoginInfoManager: LoginInfoManager

    private lateinit var baseUrlProvider: ImageUrlProviderImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        baseUrlProvider = ImageUrlProviderImpl(
            mockProjectLocalDataSource,
            mockLoginInfoManager
        )
    }

    @Test
    fun shouldReturnImageStorageBucketUrlFromProjectLocalDataSource() = runTest {
        val expected = "mock-bucket-url"
        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns "mock-project-id"
        coEvery {
            mockProjectLocalDataSource.load(any())
        } returns Project(
            "id",
            "name",
            "description",
            "creator",
            expected
        )

        assertThat(baseUrlProvider.getImageStorageBucketUrl()).isEqualTo(expected)
    }

    @Test
    fun whenNoValueIsFoundInLoginInfoManager_shouldReturnNull() = runTest {
        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns ""

        assertThat(baseUrlProvider.getImageStorageBucketUrl()).isNull()
    }

    @Test
    fun whenNoValueIsFoundInProjectLocalDataSource_shouldReturnDefaultImageStorageBucketUrl() {
        every { mockLoginInfoManager.getSignedInProjectIdOrEmpty() } returns "mock-project-id"
        coEvery { mockProjectLocalDataSource.load(any()) } returns null

        val expected = "gs://mock-project-id-images-eu"
        runTest {
            assertThat(baseUrlProvider.getImageStorageBucketUrl()).isEqualTo(expected)
        }
    }
}
