package com.simprints.infra.config.store.local

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.core.tools.utils.LanguageHelper
import com.simprints.infra.config.store.local.models.toDomain
import com.simprints.infra.config.store.local.serializer.DeviceConfigurationSerializer
import com.simprints.infra.config.store.local.serializer.ProjectConfigurationSerializer
import com.simprints.infra.config.store.local.serializer.ProjectSerializer
import com.simprints.infra.config.store.models.DeviceConfiguration
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.config.store.testtools.consentConfiguration
import com.simprints.infra.config.store.testtools.faceConfiguration
import com.simprints.infra.config.store.testtools.generalConfiguration
import com.simprints.infra.config.store.testtools.identificationConfiguration
import com.simprints.infra.config.store.testtools.project
import com.simprints.infra.config.store.testtools.projectConfiguration
import com.simprints.infra.config.store.testtools.synchronizationConfiguration
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ConfigLocalDataSourceImplTest {
    companion object {
        private const val TEST_PROJECT_DATASTORE_NAME: String = "test_project_datastore"
        private const val TEST_CONFIG_DATASTORE_NAME: String = "test_config_datastore"
        private const val TEST_DEVICE_CONFIG_DATASTORE_NAME: String = "test_device_config_datastore"
        private const val ABSOLUTE_PATH = "test"
        private const val PROJECT_ID = "projectId"
        private const val LANGUAGE = "en"
        private const val PRIVACY_NOTICE = "privacy"
    }

    private val testContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val testProjectDataStore = DataStoreFactory.create(
        serializer = ProjectSerializer,
        produceFile = { testContext.dataStoreFile(TEST_PROJECT_DATASTORE_NAME) },
    )
    private val testProjectConfigDataStore = DataStoreFactory.create(
        serializer = ProjectConfigurationSerializer,
        produceFile = { testContext.dataStoreFile(TEST_CONFIG_DATASTORE_NAME) },
    )
    private val testDeviceConfigDataStore = DataStoreFactory.create(
        serializer = DeviceConfigurationSerializer,
        produceFile = { testContext.dataStoreFile(TEST_DEVICE_CONFIG_DATASTORE_NAME) },
    )

    private val tokenizationProcessor = mockk<TokenizationProcessor>(relaxed = true)


    private lateinit var configLocalDataSourceImpl: ConfigLocalDataSourceImpl

    @Before
    fun setup() {
        LanguageHelper.init(mockk(relaxed = true))

        configLocalDataSourceImpl = ConfigLocalDataSourceImpl(
            ABSOLUTE_PATH,
            testProjectDataStore,
            testProjectConfigDataStore,
            testDeviceConfigDataStore,
            tokenizationProcessor
        )
    }

    @After
    fun teardown() {
        File(ABSOLUTE_PATH).deleteRecursively()
    }

    @Test
    fun `should throw a NoSuchElementException when there is no project`() = runTest(
        UnconfinedTestDispatcher(),
    ) {
        assertThrows<NoSuchElementException> {
            configLocalDataSourceImpl.getProject()
        }
    }

    @Test
    fun `should save the project correctly`() = runTest {
        val projectToSave = project

        configLocalDataSourceImpl.saveProject(projectToSave)
        val savedProject = configLocalDataSourceImpl.getProject()

        assertThat(savedProject).isEqualTo(projectToSave)
    }

    @Test
    fun `should clear the project correctly`() = runTest {
        configLocalDataSourceImpl.saveProject(project)
        configLocalDataSourceImpl.clearProject()

        assertThrows<NoSuchElementException> { configLocalDataSourceImpl.getProject() }
    }

    @Test
    fun `should save the project configuration and update the device configuration correctly`() = runTest {
        val projectConfigurationToSave = projectConfiguration

        configLocalDataSourceImpl.saveProjectConfiguration(projectConfigurationToSave)
        val savedProjectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
        val updatedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
        val expectedDeviceConfiguration = DeviceConfiguration(
            language = projectConfiguration.general.defaultLanguage,
            selectedModules = listOf(),
            lastInstructionId = "",
        )
        assertThat(savedProjectConfiguration).isEqualTo(projectConfiguration)
        assertThat(updatedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should save the project configuration and only update the device configuration fingersToCollect if the device configuration has been overwritten for language`() =
        runTest {
            configLocalDataSourceImpl.updateDeviceConfiguration {
                it.apply {
                    it.language = "fr"
                    it.selectedModules = listOf("module1".asTokenizableEncrypted())
                }
            }
            val projectConfigurationToSave = projectConfiguration

            configLocalDataSourceImpl.saveProjectConfiguration(projectConfigurationToSave)
            val savedProjectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
            val updatedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
            val expectedDeviceConfiguration = DeviceConfiguration(
                language = "fr",
                selectedModules = listOf("module1".asTokenizableEncrypted()),
                lastInstructionId = "",
            )
            assertThat(savedProjectConfiguration).isEqualTo(projectConfiguration)
            assertThat(updatedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
        }

    @Test
    fun `should save the project configuration and only update the device configuration language if the device configuration has been overwritten for fingersToCollect`() =
        runTest {
            configLocalDataSourceImpl.updateDeviceConfiguration {
                it.apply {
                    it.selectedModules = listOf("module1".asTokenizableEncrypted())
                }
            }
            val projectConfigurationToSave = projectConfiguration

            configLocalDataSourceImpl.saveProjectConfiguration(projectConfigurationToSave)
            val savedProjectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
            val updatedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
            val expectedDeviceConfiguration = DeviceConfiguration(
                language = projectConfiguration.general.defaultLanguage,
                selectedModules = listOf("module1".asTokenizableEncrypted()),
                lastInstructionId = "",
            )
            assertThat(savedProjectConfiguration).isEqualTo(projectConfiguration)
            assertThat(updatedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
        }

    @Test
    fun `should save the project configuration and update the device configuration correctly with an empty list of fingersToCollect if fingerprint config is missing`() =
        runTest {
            val projectConfigurationToSave = ProjectConfiguration(
                "id",
                "projectId",
                "updatedAt",
                generalConfiguration,
                faceConfiguration,
                null,
                consentConfiguration,
                identificationConfiguration,
                synchronizationConfiguration,
                null,
            )

            configLocalDataSourceImpl.saveProjectConfiguration(projectConfigurationToSave)
            val savedProjectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
            val updatedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
            val expectedDeviceConfiguration = DeviceConfiguration(
                language = generalConfiguration.defaultLanguage,
                selectedModules = listOf(),
                lastInstructionId = "",
            )
            assertThat(savedProjectConfiguration).isEqualTo(projectConfigurationToSave)
            assertThat(updatedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
        }

    @Test
    fun `should return the default configuration when there is not configuration saved`() = runTest {
        val projectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
        assertThat(projectConfiguration).isEqualTo(ConfigLocalDataSourceImpl.defaultProjectConfiguration.toDomain())
    }

    @Test
    fun `should clear the project configuration correctly`() = runTest {
        configLocalDataSourceImpl.saveProjectConfiguration(projectConfiguration)
        configLocalDataSourceImpl.clearProjectConfiguration()

        val savedProjectConfiguration = configLocalDataSourceImpl.getProjectConfiguration()
        assertThat(savedProjectConfiguration.projectId).isEqualTo("")
    }

    @Test
    fun `should update the device configuration correctly`() = runTest {
        configLocalDataSourceImpl.updateDeviceConfiguration {
            it.apply {
                it.language = "fr"
            }
        }
        val savedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
        val expectedDeviceConfiguration =
            DeviceConfiguration("fr", listOf(), "")

        assertThat(savedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should update the fingers to collect in the device configuration correctly`() = runTest {
        configLocalDataSourceImpl.updateDeviceConfiguration {
            it.apply {
                it.language = "fr"
            }
        }
        var savedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
        var expectedDeviceConfiguration =
            DeviceConfiguration("fr", listOf(), "")

        assertThat(savedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)

        configLocalDataSourceImpl.updateDeviceConfiguration {
            it.apply {
                it.language = "fr"
            }
        }
        savedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
        expectedDeviceConfiguration =
            DeviceConfiguration(
                "fr",
                listOf(),
                "",
            )

        assertThat(savedDeviceConfiguration).isEqualTo(expectedDeviceConfiguration)
    }

    @Test
    fun `should clear the device configuration correctly`() = runTest {
        configLocalDataSourceImpl.updateDeviceConfiguration { it.apply { it.language = "fr" } }
        configLocalDataSourceImpl.clearDeviceConfiguration()

        val savedDeviceConfiguration = configLocalDataSourceImpl.getDeviceConfiguration()
        assertThat(savedDeviceConfiguration.language).isEqualTo("")
    }

    @Test
    fun `should store the privacy notice correctly`() {
        configLocalDataSourceImpl.storePrivacyNotice(PROJECT_ID, LANGUAGE, PRIVACY_NOTICE)

        val privacyNotice = configLocalDataSourceImpl.getPrivacyNotice(PROJECT_ID, LANGUAGE)
        assertThat(privacyNotice).isEqualTo(PRIVACY_NOTICE)
    }

    @Test
    fun `hasPrivacyNoticeFor should return false if the privacy notice doesn't exist`() {
        val exist = configLocalDataSourceImpl.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE)
        assertThat(exist).isEqualTo(false)
    }

    @Test
    fun `hasPrivacyNoticeFor should return true if the privacy notice exists`() {
        configLocalDataSourceImpl.storePrivacyNotice(PROJECT_ID, LANGUAGE, PRIVACY_NOTICE)

        val exist = configLocalDataSourceImpl.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE)
        assertThat(exist).isEqualTo(true)
    }

    @Test
    fun `deletePrivacyNotices should delete all the privacy notices`() {
        configLocalDataSourceImpl.storePrivacyNotice(PROJECT_ID, LANGUAGE, PRIVACY_NOTICE)
        var exist = configLocalDataSourceImpl.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE)
        assertThat(exist).isEqualTo(true)

        configLocalDataSourceImpl.deletePrivacyNotices()

        exist = configLocalDataSourceImpl.hasPrivacyNoticeFor(PROJECT_ID, LANGUAGE)
        assertThat(exist).isEqualTo(false)
    }

    @Test
    fun `watchProjectConfiguration should emit updated values when configuration changes`() = runTest {
        val config1 = projectConfiguration.copy(projectId = "project1")
        val config2 = projectConfiguration.copy(projectId = "project2")
        val config3 = projectConfiguration.copy(projectId = "project3")
        val config4 = projectConfiguration.copy(projectId = "project4")
        val emittedConfigs = mutableListOf<ProjectConfiguration>()

        configLocalDataSourceImpl.saveProjectConfiguration(config1)
        configLocalDataSourceImpl.saveProjectConfiguration(config2) // will replay when collection starts below

        val job = launch {
            configLocalDataSourceImpl.watchProjectConfiguration().collect { emittedConfigs.add(it) }
        }

        configLocalDataSourceImpl.saveProjectConfiguration(config3)
        configLocalDataSourceImpl.saveProjectConfiguration(config4)

        assertThat(emittedConfigs).hasSize(3)
        assertThat(emittedConfigs[0]).isEqualTo(config2)
        assertThat(emittedConfigs[1]).isEqualTo(config3)
        assertThat(emittedConfigs[2]).isEqualTo(config4)
        job.cancel()
    }
}
