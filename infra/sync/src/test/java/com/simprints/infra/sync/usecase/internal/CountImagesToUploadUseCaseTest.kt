package com.simprints.infra.sync.usecase.internal

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.images.ImageRepository
import com.simprints.infra.sync.config.testtools.projectConfiguration
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountImagesToUploadUseCaseTest {
    @MockK
    private lateinit var configRepository: ConfigRepository

    @MockK
    private lateinit var imageRepository: ImageRepository

    private lateinit var useCase: CountImagesToUploadUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        useCase = CountImagesToUploadUseCase(configRepository, imageRepository)
    }

    @Test
    fun `maps project id to observed image upload count`() = runTest {
        val configFlow = MutableSharedFlow<ProjectConfiguration>()
        val imageCountFlow = MutableSharedFlow<Int>()
        every { configRepository.observeProjectConfiguration() } returns configFlow
        coEvery { imageRepository.observeNumberOfImagesToUpload(PROJECT_ID) } returns imageCountFlow
        val emitted = mutableListOf<Int>()

        val collectJob = launch { useCase().take(2).toList(emitted) }

        runCurrent()
        configFlow.emit(projectConfiguration.copy(projectId = PROJECT_ID))
        runCurrent()
        imageCountFlow.emit(0)
        imageCountFlow.emit(5)
        runCurrent()
        collectJob.join()
        assertThat(emitted).containsExactly(0, 5).inOrder()
        verify(exactly = 1) { configRepository.observeProjectConfiguration() }
        coVerify(exactly = 1) { imageRepository.observeNumberOfImagesToUpload(PROJECT_ID) }
    }

    @Test
    fun `switches to latest project image count flow when project id changes`() = runTest {
        val configFlow = MutableSharedFlow<ProjectConfiguration>()
        val imageCountFlow1 = MutableSharedFlow<Int>()
        val imageCountFlow2 = MutableSharedFlow<Int>()
        every { configRepository.observeProjectConfiguration() } returns configFlow
        coEvery { imageRepository.observeNumberOfImagesToUpload(PROJECT_ID_1) } returns imageCountFlow1
        coEvery { imageRepository.observeNumberOfImagesToUpload(PROJECT_ID_2) } returns imageCountFlow2
        val emitted = mutableListOf<Int>()

        val collectJob = launch { useCase().take(2).toList(emitted) }
        runCurrent()

        configFlow.emit(projectConfiguration.copy(projectId = PROJECT_ID_1))
        runCurrent()

        imageCountFlow1.emit(1)
        runCurrent()

        configFlow.emit(projectConfiguration.copy(projectId = PROJECT_ID_2))
        runCurrent()

        imageCountFlow1.emit(2) // should be ignored due to flatMapLatest
        imageCountFlow2.emit(3)
        runCurrent()

        collectJob.join()
        assertThat(emitted).containsExactly(1, 3).inOrder()
        coVerify(exactly = 1) { imageRepository.observeNumberOfImagesToUpload(PROJECT_ID_1) }
        coVerify(exactly = 1) { imageRepository.observeNumberOfImagesToUpload(PROJECT_ID_2) }
    }

    companion object {
        private const val PROJECT_ID = "project-id"
        private const val PROJECT_ID_1 = "project-id-1"
        private const val PROJECT_ID_2 = "project-id-2"
    }
}
