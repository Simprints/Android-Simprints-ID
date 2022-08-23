package com.simprints.infra.config.local

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.local.models.toDomain
import com.simprints.infra.config.local.serializer.DeviceConfigurationSerializer
import com.simprints.infra.config.local.serializer.ProjectConfigurationSerializer
import com.simprints.infra.config.local.serializer.ProjectSerializer
import com.simprints.infra.config.testtools.project
import com.simprints.infra.config.testtools.projectConfiguration
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigLocalDataSourceImplTest {

    companion object {
        private const val TEST_PROJECT_DATASTORE_NAME: String = "test_project_datastore"
        private const val TEST_CONFIG_DATASTORE_NAME: String = "test_config_datastore"
        private const val TEST_DEVICE_CONFIG_DATASTORE_NAME: String = "test_device_config_datastore"
    }

    private val testContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val testProjectDataStore = DataStoreFactory.create(
        serializer = ProjectSerializer,
        produceFile = { testContext.dataStoreFile(TEST_PROJECT_DATASTORE_NAME) }
    )
    private val testProjectConfigDataStore = DataStoreFactory.create(
        serializer = ProjectConfigurationSerializer,
        produceFile = { testContext.dataStoreFile(TEST_CONFIG_DATASTORE_NAME) }
    )
    private val testDeviceConfigDataStore = DataStoreFactory.create(
        serializer = DeviceConfigurationSerializer,
        produceFile = { testContext.dataStoreFile(TEST_DEVICE_CONFIG_DATASTORE_NAME) }
    )
    private val configLocalDataSourceImpl =
        ConfigLocalDataSourceImpl(
            testProjectDataStore,
            testProjectConfigDataStore,
            testDeviceConfigDataStore
        )

    @After
    fun teardown() = runTest(UnconfinedTestDispatcher()) {
        testProjectDataStore.updateData { it.toBuilder().clear().build() }
    }

    @Test
    fun `should throw a NoSuchElementException when there is no project`() = runTest(
        UnconfinedTestDispatcher()
    ) {
        assertThrows<NoSuchElementException> {
            configLocalDataSourceImpl.getProject()
        }
    }

    @Test
    fun `should save the project correctly`() = runTest(UnconfinedTestDispatcher()) {
        val projectToSave = project

        configLocalDataSourceImpl.saveProject(projectToSave)
        val savedProject = configLocalDataSourceImpl.getProject()

        assertThat(savedProject).isEqualTo(projectToSave)
    }

    @Test
    fun `should save the project configuration correctly`() = runTest(UnconfinedTestDispatcher()) {
        val projectConfigurationToSave = projectConfiguration

        configLocalDataSourceImpl.saveProjectConfiguration(projectConfigurationToSave)
        val savedProjectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()

        assertThat(savedProjectConfiguration).isEqualTo(projectConfiguration)
    }

    @Test
    fun `should return the default configuration when there is not configuration saved`() =
        runTest(UnconfinedTestDispatcher()) {
            val projectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
            assertThat(projectConfiguration).isEqualTo(ConfigLocalDataSourceImpl.defaultProjectConfiguration.toDomain())
        }

    @Test
    fun `should update the device configuration correctly`() = runTest(UnconfinedTestDispatcher()) {
        configLocalDataSourceImpl.updateDeviceConfiguration {
            it.apply { it.language = "fr" }
        }
        val savedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
        val expectedDeviceConfiguration = DeviceConfiguration("fr", listOf())

        assertThat(savedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }
}
