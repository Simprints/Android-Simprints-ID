package com.simprints.infra.config.local

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.local.models.ProjectSerializer
import com.simprints.infra.config.testtools.project
import com.simprints.testtools.common.syntax.assertThrows
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfigLocalDataSourceImplTest {

    companion object {
        private const val TEST_DATASTORE_NAME: String = "test_datastore"
    }

    private val testContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val testProjectDataStore = DataStoreFactory.create(
        serializer = ProjectSerializer,
        produceFile = { testContext.dataStoreFile(TEST_DATASTORE_NAME) }
    )
    private val configLocalDataSourceImpl = ConfigLocalDataSourceImpl(testProjectDataStore)

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
        val project = project

        configLocalDataSourceImpl.saveProject(project)
        val savedProject = configLocalDataSourceImpl.getProject()

        assertThat(savedProject).isEqualTo(project)
    }
}
