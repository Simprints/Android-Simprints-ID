package com.simprints.feature.clientapi.usecases

import com.google.common.truth.Truth.assertThat
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.config.store.models.UpSynchronizationConfiguration
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetEnrolmentCreationEventForSubjectUseCaseTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var configManager: ConfigManager

    @MockK
    private lateinit var enrolmentRecordManager: EnrolmentRecordManager

    @MockK
    private lateinit var encoder: EncodingUtils

    @MockK
    private lateinit var jsonHelper: JsonHelper

    private lateinit var useCase: GetEnrolmentCreationEventForSubjectUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        every { jsonHelper.toJson(any()) } returns "json"

        useCase = GetEnrolmentCreationEventForSubjectUseCase(configManager, enrolmentRecordManager, encoder, jsonHelper)
    }

    @Test
    fun `returns null if coSync disabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.NONE
        }

        val result = useCase("projectId", "subjectId")

        coVerify(exactly = 0) { enrolmentRecordManager.load(any()) }
        assertThat(result).isNull()
    }

    @Test
    fun `returns null if only analytics sync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_ANALYTICS
        }

        val result = useCase("projectId", "subjectId")

        coVerify(exactly = 0) { enrolmentRecordManager.load(any()) }
        assertThat(result).isNull()
    }

    @Test
    fun `returns null if no creation event`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
        }

        coEvery { enrolmentRecordManager.load(any()) } returns emptyFlow()

        val result = useCase("projectId", "subjectId")

        assertThat(result).isNull()
    }

    @Test
    fun `returns event if biometrics coSync enabled`() = runTest {
        coEvery { configManager.getProjectConfiguration() } returns mockk {
            every { synchronization.up.coSync.kind } returns UpSynchronizationConfiguration.UpSynchronizationKind.ONLY_BIOMETRICS
        }

        coEvery { enrolmentRecordManager.load(any()) } returns flowOf(mockk(relaxed = true))

        val result = useCase("projectId", "subjectId")

        coVerify { enrolmentRecordManager.load(any()) }
        coVerify { jsonHelper.toJson(any()) }
        assertThat(result).isNotNull()
    }
}
