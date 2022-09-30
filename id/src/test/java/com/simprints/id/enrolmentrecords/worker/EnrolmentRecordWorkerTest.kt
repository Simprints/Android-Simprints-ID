package com.simprints.id.enrolmentrecords.worker

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.enrolmentrecords.EnrolmentRecordRepository
import com.simprints.id.testtools.TestApplication
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import com.simprints.testtools.common.coroutines.TestDispatcherProvider
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class EnrolmentRecordWorkerTest {

    companion object {
        private const val INSTRUCTION_ID = "id"
        private const val SUBJECT_ID = "subjectId"
    }

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val repository = mockk<EnrolmentRecordRepository>()
    private val settingsPreferencesManager = mockk<SettingsPreferencesManager>()

    @Before
    fun setUp() {
        app.component = mockk(relaxed = true)
    }

    @Test
    fun `should do work correctly`() = runTest {
        val data = workDataOf(
            EnrolmentRecordSchedulerImpl.INPUT_ID_NAME to INSTRUCTION_ID,
            EnrolmentRecordSchedulerImpl.INPUT_SUBJECT_IDS_NAME to arrayOf(SUBJECT_ID),
        )
        val worker = createWorker(data)

        worker.doWork()

        coVerify(exactly = 1) { repository.uploadRecords(listOf(SUBJECT_ID)) }
        verify(exactly = 1) {
            settingsPreferencesManager.setProperty("lastInstructionId").value(INSTRUCTION_ID)
        }
    }

    private fun createWorker(data: Data): EnrolmentRecordWorker =
        TestListenableWorkerBuilder<EnrolmentRecordWorker>(app, data).build().apply {
            repository = this@EnrolmentRecordWorkerTest.repository
            settingsPreferencesManager = this@EnrolmentRecordWorkerTest.settingsPreferencesManager
            dispatcherProvider = TestDispatcherProvider(testCoroutineRule)
        }
}
