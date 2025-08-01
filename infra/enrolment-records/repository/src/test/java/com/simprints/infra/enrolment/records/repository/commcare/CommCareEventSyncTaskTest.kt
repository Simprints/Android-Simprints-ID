package com.simprints.infra.enrolment.records.repository.commcare

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.core.net.toUri
import com.simprints.infra.enrolment.records.repository.commcare.domain.CommCareCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommCareEventSyncTaskTest {

    private lateinit var syncTask: CommCareEventSyncTask
    private val mockContext: Context = mockk()
    private val mockContentResolver: ContentResolver = mockk()
    private val mockCommCareCaseRepository: CommCareCaseRepository = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testPackageName = "org.commcare.dalvik"
    private val testCaseId = "case123"
    private val testSubjectId = "subject456"
    private val testLastModified = 1640995200000L

    @Before
    fun setup() {
        every { mockContext.contentResolver } returns mockContentResolver
        
        syncTask = CommCareEventSyncTask(
            context = mockContext,
            commCareCaseRepository = mockCommCareCaseRepository,
            dispatcher = testDispatcher,
        )
        
        // Mock Uri.toUri() extension
        mockkStatic("androidx.core.net.UriKt")
    }

    @Test
    fun `syncCommCareCases should process cases successfully`() = runTest {
        // Setup mock cursors
        val mockMetadataCursor = mockk<Cursor>()
        val mockDataCursor = mockk<Cursor>()
        val mockMetadataUri = mockk<Uri>()
        val mockDataUri = mockk<Uri>()
        val mockDataCaseUri = mockk<Uri>()
        val mockUriBuilder = mockk<Uri.Builder>()

        // Mock URI creation
        every { "content://$testPackageName.case/casedb/case".toUri() } returns mockMetadataUri
        every { "content://$testPackageName.case/casedb/data".toUri() } returns mockDataUri
        every { mockDataUri.buildUpon() } returns mockUriBuilder
        every { mockUriBuilder.appendPath(testCaseId) } returns mockUriBuilder
        every { mockUriBuilder.build() } returns mockDataCaseUri

        // Mock metadata cursor
        every { mockContentResolver.query(mockMetadataUri, null, null, null, null) } returns mockMetadataCursor
        every { mockMetadataCursor.use<Cursor, Any>(any()) } answers {
            val block = arg<(Cursor) -> Any>(0)
            block(mockMetadataCursor)
        }
        every { mockMetadataCursor.getColumnIndex("case_id") } returns 0
        every { mockMetadataCursor.getColumnIndex("last_modified") } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(0) } returns testCaseId
        every { mockMetadataCursor.getLong(1) } returns testLastModified

        // Mock data cursor
        every { mockContentResolver.query(mockDataCaseUri, null, null, null, null) } returns mockDataCursor
        every { mockDataCursor.use<Cursor, Any>(any()) } answers {
            val block = arg<(Cursor) -> Any>(0)
            block(mockDataCursor)
        }
        every { mockDataCursor.getColumnIndex("datum_id") } returns 0
        every { mockDataCursor.getColumnIndex("value") } returns 1
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(0) } returns "simprintsId"
        every { mockDataCursor.getString(1) } returns testSubjectId

        // Mock repository
        coEvery { mockCommCareCaseRepository.saveCase(any()) } returns Unit

        // Execute
        val result = syncTask.syncCommCareCases(testPackageName)

        // Verify
        val expectedCase = CommCareCase(testCaseId, testSubjectId, testLastModified)
        coVerify { mockCommCareCaseRepository.saveCase(expectedCase) }
        assertEquals(1, result.size)
        assertEquals(expectedCase, result[0])
    }

    @Test
    fun `syncCommCareCases should handle empty results gracefully`() = runTest {
        val mockMetadataUri = mockk<Uri>()
        
        every { "content://$testPackageName.case/casedb/case".toUri() } returns mockMetadataUri
        every { mockContentResolver.query(mockMetadataUri, null, null, null, null) } returns null

        val result = syncTask.syncCommCareCases(testPackageName)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `deleteCaseBySubjectId should call repository delete`() = runTest {
        coEvery { mockCommCareCaseRepository.deleteCaseBySubjectId(testSubjectId) } returns Unit

        syncTask.deleteCaseBySubjectId(testSubjectId)

        coVerify { mockCommCareCaseRepository.deleteCaseBySubjectId(testSubjectId) }
    }
}