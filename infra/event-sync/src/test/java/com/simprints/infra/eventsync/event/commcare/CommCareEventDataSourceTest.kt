package com.simprints.infra.eventsync.event.commcare

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import androidx.preference.PreferenceManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_CASE_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_DATUM_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_VALUE
import com.simprints.infra.logging.Simber
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class CommCareEventDataSourceTest {
    companion object {
        private const val TEST_PACKAGE_NAME = "org.commcare.dalvik"
        private const val SUBJECT_ACTIONS_EVENT_1 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"123","finger":"LEFT_THUMB"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_EVENT_2 =
            """{"events":[{"id":"1eafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":88,"template":"456","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val INVALID_JSON = """{"invalid": json"""

        @get:Rule
        val testCoroutineRule = TestCoroutineRule()

        @JvmStatic
        lateinit var mockMetadataUri: Uri

        @JvmStatic
        lateinit var mockDataUri: Uri

        @JvmStatic
        lateinit var mockDataCaseIdUri: Uri

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            mockkObject(Simber)
            mockMetadataUri = mockk(relaxed = true)
            mockDataUri = mockk(relaxed = true)
            mockDataCaseIdUri = mockk(relaxed = true)
            mockkStatic(Uri::class)
            mockkStatic(PreferenceManager::class)
            every { Uri.parse("content://$TEST_PACKAGE_NAME.case/casedb/case") } returns mockMetadataUri
            every { Uri.parse("content://$TEST_PACKAGE_NAME.case/casedb/data") } returns mockDataUri
            every { mockDataUri.buildUpon().appendPath(any()).build() } returns mockDataCaseIdUri
        }

        @JvmStatic
        @AfterClass
        fun cleanupClass() {
            clearAllMocks()
            unmockkAll()
            unmockkStatic(Uri::class)
            unmockkStatic(PreferenceManager::class)
        }
    }

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockContentResolver: ContentResolver

    @MockK
    private lateinit var mockSharedPreferences: SharedPreferences

    private lateinit var mockMetadataCursor: Cursor

    private lateinit var mockDataCursor: Cursor

    private lateinit var dataSource: CommCareEventDataSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver } returns mockContentResolver
        every { context.getString(any()) } returns TEST_PACKAGE_NAME
        every { PreferenceManager.getDefaultSharedPreferences(context) } returns mockSharedPreferences
        every { mockSharedPreferences.getString(any(), any()) } returns TEST_PACKAGE_NAME

        every { Uri.parse(any()) } answers {
            val uriPath = it.invocation.args[0] as String
            if (uriPath.endsWith("case")) mockMetadataUri else mockDataUri
        }

        mockMetadataCursor = mockk(relaxed = true)
        mockDataCursor = mockk(relaxed = true)

        every { mockMetadataCursor.close() } just Runs
        every { mockDataCursor.close() } just Runs

        every {
            mockContentResolver.query(
                mockMetadataUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns mockMetadataCursor

        every {
            mockContentResolver.query(
                mockDataCaseIdUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns mockDataCursor

        dataSource = CommCareEventDataSource(
            JsonHelper,
            context,
        )
    }

    @Test
    fun `getEvents returns correct count and event flow`() = runTest {
        val expectedCount = 2
        every { mockMetadataCursor.count } returns expectedCount
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returnsMany listOf("case1", "case2")

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany listOf("subjectActions", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany listOf(SUBJECT_ACTIONS_EVENT_1, SUBJECT_ACTIONS_EVENT_2)

        val result = dataSource.getEvents()

        assertEquals(expectedCount, result.totalCount)
        val events = result.eventFlow.toList()
        assertEquals(2, events.size)
        assertEquals("b26c91bc-b307-4131-80c3-55090ba5dbf2", (events[0] as? EnrolmentRecordCreationEvent)?.payload?.subjectId)
        assertEquals("a961fcb4-8573-4270-a1b2-088e88275b00", (events[1] as? EnrolmentRecordCreationEvent)?.payload?.subjectId)

        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `count returns correct value when cursor is available`() {
        val expectedCount = 5
        every { mockMetadataCursor.count } returns expectedCount

        val result = dataSource.getEvents()

        assertEquals(expectedCount, result.totalCount)
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `count returns zero when cursor is null`() {
        every {
            mockContentResolver.query(
                mockMetadataUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns null

        val result = dataSource.getEvents()

        assertEquals(0, result.totalCount)
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `loadEnrolmentRecordCreationEvents handles empty case list`() = runTest {
        every { mockMetadataCursor.count } returns 0
        every { mockMetadataCursor.moveToNext() } returns false

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(0, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `loadEnrolmentRecordCreationEvents handles null case id`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns null

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `loadEnrolmentRecordCreationEvents throws exception when data cursor is null`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case1"
        every {
            mockContentResolver.query(
                mockDataCaseIdUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns null

        val result = dataSource.getEvents()

        try {
            result.eventFlow.toList()
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("Cursor for caseId case1 is null", e.message)
        }

        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `getSubjectActionsValue returns empty string when subjectActions not found`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case1"

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany listOf("someOtherKey", "anotherKey")
        every { mockDataCursor.getString(1) } returnsMany listOf("someValue", "anotherValue")

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `parseRecordEvents handles invalid JSON gracefully`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case1"

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns INVALID_JSON

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size)
        verify { Simber.e(any(), ofType<Exception>()) }
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `parseRecordEvents handles empty subjectActions`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case1"

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns ""

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `loadEnrolmentRecordCreationEvents processes events in batches`() = runTest {
        val caseCount = 25 // More than batch size of 20
        every { mockMetadataCursor.count } returns caseCount

        // Mock cursor to return case IDs
        val moveNextResults = (1..caseCount).map { true } + false
        every { mockMetadataCursor.moveToNext() } returnsMany moveNextResults

        val caseIds = (1..caseCount).map { "case$it" }
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returnsMany caseIds

        // Mock data cursor for each case
        every { mockDataCursor.moveToNext() } returnsMany moveNextResults
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(caseCount, result.totalCount)
        assertEquals(caseCount, events.size) // One event per case
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify(exactly = caseCount) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `exception during metadata cursor query is propagated`() = runTest {
        every {
            mockContentResolver.query(
                mockMetadataUri,
                arrayOf(COLUMN_CASE_ID),
                any(),
                any(),
                any(),
            )
        } throws RuntimeException("Database error")

        val result = dataSource.getEvents()

        try {
            result.eventFlow.toList()
            assert(false) { "Expected RuntimeException" }
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }

        verify { Simber.e(any(), ofType<RuntimeException>()) }
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID), any(), any(), any()) }
    }

    @Test
    fun `getPackageName uses preference value when available`() {
        val customPackageName = "custom.commcare.package"
        every { mockSharedPreferences.getString(any(), any()) } returns customPackageName

        // We can't directly test private methods, but we can verify the URI creation behavior
        every { Uri.parse("content://$customPackageName.case/casedb/case") } returns mockMetadataUri
        every { mockMetadataCursor.count } returns 0

        val result = dataSource.getEvents()

        assertEquals(0, result.totalCount)
        verify { mockSharedPreferences.getString(any(), any()) }
    }

    @Test
    fun `getPackageName falls back to default when preference is null`() {
        every { mockSharedPreferences.getString(any(), any()) } returns null

        // The fallback should use the default value passed to getString
        every { mockMetadataCursor.count } returns 0

        val result = dataSource.getEvents()

        assertEquals(0, result.totalCount)
        verify { mockSharedPreferences.getString(any(), any()) }
    }
}
