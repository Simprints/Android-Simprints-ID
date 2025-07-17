import android.content.Intent
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.*
import com.google.common.truth.Truth.*
import com.simprints.feature.datagenerator.DataGeneratorViewModel
import com.simprints.feature.datagenerator.enrollmentrecords.InsertEnrollmentRecordsUseCase
import com.simprints.infra.authstore.AuthStore
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class DataGeneratorViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DataGeneratorViewModel

    @MockK
    private lateinit var insertEnrollmentRecordsUseCase: InsertEnrollmentRecordsUseCase

    @MockK
    private lateinit var authStore: AuthStore

    companion object {
        // Private constants matching those in the ViewModel for test readability
        private const val ACTION_GENERATE_ENROLLMENT_RECORDS = "com.simprints.test.GENERATE_ENROLLMENT_RECORDS"

        private const val EXTRA_PROJECT_ID = "EXTRA_PROJECT_ID"
        private const val EXTRA_MODULE_ID = "EXTRA_MODULE_ID"
        private const val EXTRA_ATTENDANT_ID = "EXTRA_ATTENDANT_ID"
        private const val EXTRA_NUM_RECORDS = "EXTRA_NUM_RECORDS"
        private const val EXTRA_TEMPLATES_PER_FORMAT = "EXTRA_TEMPLATES_PER_FORMAT"
        private const val EXTRA_FIRST_SUBJECT_ID = "EXTRA_FIRST_SUBJECT_ID"
        private const val EXTRA_FINGER_ORDER = "EXTRA_FINGER_ORDER"
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { authStore.signedInProjectId } returns "test_project_id"
        viewModel = DataGeneratorViewModel(insertEnrollmentRecordsUseCase, authStore)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleIntent with null intent does not trigger any action`() = runTest {
        // When
        viewModel.handleIntent(null)

        // Then throws IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleIntent with null action does not trigger any action`() = runTest {
        // Given
        val intent = Intent() // Intent with no action

        // When
        viewModel.handleIntent(intent)

        // Then throws IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleIntent with unknown action does not trigger any action`() = runTest {
        // Given
        val intent = Intent("com.simprints.test.UNKNOWN_ACTION")

        // When
        viewModel.handleIntent(intent)

        // Then throws IllegalArgumentException
    }

    @Test(expected = IllegalStateException::class)
    fun `handleIntent with empty project id does not trigger any action`() = runTest {
        // Given
        val intent = Intent(ACTION_GENERATE_ENROLLMENT_RECORDS).apply {
            putExtra(EXTRA_PROJECT_ID, "proj1")
            putExtra(EXTRA_MODULE_ID, "mod1")
            putExtra(EXTRA_ATTENDANT_ID, "att1")
            putExtra(EXTRA_NUM_RECORDS, 0) // numRecords <= 0 should fail the check
        }
        every { authStore.signedInProjectId } returns ""

        // When
        viewModel.handleIntent(intent)

        // Then throws IllegalStateException
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleIntent with GENERATE_ENROLLMENT_RECORDS and missing extras does not call use case`() = runTest {
        // Given
        val intent = Intent(ACTION_GENERATE_ENROLLMENT_RECORDS)
        // Missing projectId, moduleId, attendantId, and numRecords

        // When
        viewModel.handleIntent(intent)

        // Then throws IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException::class)
    fun `handleIntent with GENERATE_ENROLLMENT_RECORDS and numRecords is 0 does not call use case`() = runTest {
        // Given
        val intent = Intent(ACTION_GENERATE_ENROLLMENT_RECORDS).apply {
            putExtra(EXTRA_PROJECT_ID, "proj1")
            putExtra(EXTRA_MODULE_ID, "mod1")
            putExtra(EXTRA_ATTENDANT_ID, "att1")
            putExtra(EXTRA_NUM_RECORDS, 0) // numRecords <= 0 should fail the check
        }

        // When
        viewModel.handleIntent(intent)

        // Then throws  IllegalArgumentException
    }

    @Test
    fun `handleIntent with GENERATE_ENROLLMENT_RECORDS and valid extras calls use case and updates status`() = runTest {
        // Given
        val projectId = "proj1"
        val moduleId = "mod1"
        val attendantId = "att1"
        val numRecords = 50
        val firstSubjectId = "sub1"
        val templatesBundle = Bundle().apply { putInt("ISO_19794_2", 2) }
        val fingerOrderBundle = Bundle().apply { putString("ISO_19794_2", "LEFT_THUMB") }

        val intent = Intent(ACTION_GENERATE_ENROLLMENT_RECORDS).apply {
            putExtra(EXTRA_PROJECT_ID, projectId)
            putExtra(EXTRA_MODULE_ID, moduleId)
            putExtra(EXTRA_ATTENDANT_ID, attendantId)
            putExtra(EXTRA_NUM_RECORDS, numRecords)
            putExtra(EXTRA_FIRST_SUBJECT_ID, firstSubjectId)
            putExtra(EXTRA_TEMPLATES_PER_FORMAT, templatesBundle)
            putExtra(EXTRA_FINGER_ORDER, fingerOrderBundle)
        }

        val expectedStatus = "Inserted 50 biometric records"
        coEvery { insertEnrollmentRecordsUseCase.invoke(any(), any(), any(), any(), any(), any(), any()) } returns flowOf(expectedStatus)

        // When
        viewModel.handleIntent(intent)

        // Then
        coVerify(exactly = 1) {
            insertEnrollmentRecordsUseCase.invoke(
                projectId = projectId,
                moduleId = moduleId,
                attendantId = attendantId,
                numRecords = numRecords,
                templatesPerFormat = templatesBundle,
                firstSubjectId = firstSubjectId,
                fingerOrder = fingerOrderBundle,
            )
        }

        val status = viewModel.statusMessage.value
        assertThat(status).isEqualTo(expectedStatus)
    }

    @Test
    fun `handleIntent with GENERATE_ENROLLMENT_RECORDS and flat extras correctly reconstructs bundles`() = runTest {
        // Given
        val templatesSlot = slot<Bundle>()
        val fingerOrderSlot = slot<Bundle>()

        val intent = Intent(ACTION_GENERATE_ENROLLMENT_RECORDS).apply {
            putExtra(EXTRA_PROJECT_ID, "proj1")
            putExtra(EXTRA_MODULE_ID, "mod1")
            putExtra(EXTRA_ATTENDANT_ID, "att1")
            putExtra(EXTRA_NUM_RECORDS, 10)
            // Flattened extras instead of a Bundle object
            putExtra("$EXTRA_TEMPLATES_PER_FORMAT.ISO_19794_2", 2)
            putExtra("$EXTRA_TEMPLATES_PER_FORMAT.SIM_FACE_BASE_1", 1)
            putExtra("$EXTRA_FINGER_ORDER.ISO_19794_2", "LEFT_THUMB,LEFT_INDEX_FINGER")
        }

        coEvery {
            insertEnrollmentRecordsUseCase.invoke(any(), any(), any(), any(), capture(templatesSlot), any(), capture(fingerOrderSlot))
        } returns flowOf("Done")

        // When
        viewModel.handleIntent(intent)

        // Then
        coVerify(exactly = 1) { insertEnrollmentRecordsUseCase.invoke(any(), any(), any(), any(), any(), any(), any()) }

        // Verify the bundle was reconstructed correctly
        val capturedTemplates = templatesSlot.captured
        assertThat(capturedTemplates.getInt("ISO_19794_2")).isEqualTo(2)
        assertThat(capturedTemplates.getInt("SIM_FACE_BASE_1")).isEqualTo(1)

        val capturedFingerOrder = fingerOrderSlot.captured
        assertThat(capturedFingerOrder.getString("ISO_19794_2")).isEqualTo("LEFT_THUMB,LEFT_INDEX_FINGER")
    }
}
