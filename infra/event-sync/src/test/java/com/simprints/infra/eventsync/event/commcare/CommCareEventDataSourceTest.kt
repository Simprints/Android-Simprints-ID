package com.simprints.infra.eventsync.event.commcare

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import androidx.preference.PreferenceManager
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.BATCH_SIZE
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_CASE_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_DATUM_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_LAST_MODIFIED
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_VALUE
import com.simprints.infra.eventsync.sync.common.EventSyncCache
import com.simprints.infra.logging.LoggingConstants
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommCareEventDataSourceTest {
    companion object {
        private const val TEST_PACKAGE_NAME = "org.commcare.dalvik"
        private const val SUBJECT_ACTIONS_EVENT_1 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"123","finger":"LEFT_THUMB"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_EVENT_2 =
            """{"events":[{"id":"1eafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":88,"template":"456","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val INVALID_JSON = """{"invalid": json"""

        private const val COLUMN_INDEX_DATUM_ID = 0 // Assuming this is the index for COLUMN_DATUM_ID in the mock cursor
        private const val COLUMN_INDEX_VALUE = 1 // Assuming this is the index for COLUMN_VALUE in the mock cursor
        private const val COLUMN_INDEX_LAST_MODIFIED = 2 // Assuming this is the index for COLUMN_LAST_MODIFIED in the mock cursor

        // Helper to format date strings as CommCare does
        private val commCareDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        private fun formatCommCareDate(millis: Long): String = commCareDateFormat.format(Date(millis))

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

    @MockK
    private lateinit var mockEventSyncCache: EventSyncCache

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
        
        // Default behavior for sync time: no previous sync
        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns null
        // Default behavior for last modified column index
        every { mockMetadataCursor.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED) } returns COLUMN_INDEX_LAST_MODIFIED
        // Default behavior for last modified time: current time, so it's always processed
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(10000L)

        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns COLUMN_INDEX_DATUM_ID
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns COLUMN_INDEX_VALUE


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
            mockEventSyncCache,
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
        every { mockDataCursor.getString(0) } returnsMany listOf("subjectActions", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany listOf(SUBJECT_ACTIONS_EVENT_1, SUBJECT_ACTIONS_EVENT_2)

        val result = dataSource.getEvents()

        assertEquals(expectedCount, result.totalCount)
        val events = result.eventFlow.toList()
        assertEquals(2, events.size)
        assertEquals("b26c91bc-b307-4131-80c3-55090ba5dbf2", (events[0] as? EnrolmentRecordCreationEvent)?.payload?.subjectId)
        assertEquals("a961fcb4-8573-4270-a1b2-088e88275b00", (events[1] as? EnrolmentRecordCreationEvent)?.payload?.subjectId)

        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify(exactly = 2) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `count returns correct value when cursor is available`() {
        val expectedCount = 5
        every { mockMetadataCursor.count } returns expectedCount

        val result = dataSource.getEvents() // count is called internally

        assertEquals(expectedCount, result.totalCount)
        verify { mockContentResolver.query(mockMetadataUri, null, null, null, null) }
    }

    @Test
    fun `count returns zero when cursor is null`() {
        every {
            mockContentResolver.query(
                mockMetadataUri,
                null, // For count() specifically
                null,
                null,
                null,
            )
        } returns null

        val result = dataSource.getEvents()

        assertEquals(0, result.totalCount)
        verify { mockContentResolver.query(mockMetadataUri, null, null, null, null) }
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
        assertEquals(0, events.size) // No caseId, so no events processed
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `loadEnrolmentRecordCreationEvents throws exception when data cursor is null`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case1"
        every {
            mockContentResolver.query(
                mockDataCaseIdUri, // It will be mockDataUri.buildUpon().appendPath("case1").build()
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
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns INVALID_JSON

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // Event parsing fails, so no event emitted
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
        val caseCount = BATCH_SIZE + 5
        every { mockMetadataCursor.count } returns caseCount

        val moveNextResultsMetadata = (1..caseCount).map { true } + false
        every { mockMetadataCursor.moveToNext() } returnsMany moveNextResultsMetadata
        val caseIds = (1..caseCount).map { "case$it" }
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returnsMany caseIds

        // Mock data cursor for each case, assuming one subjectActions per case
        val moveNextResultsData = (1..caseCount).map { true } + List(caseCount) {false} // true then false for each caseId
        every { mockDataCursor.moveToNext() } returnsMany moveNextResultsData
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
    fun `Task skips events modified before last sync time`() = runTest {
        val lastSyncTimeMs = 10000L
        val eventTimeMs = lastSyncTimeMs - 1000L // 1 second before

        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns Timestamp(lastSyncTimeMs)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case_old"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(eventTimeMs)

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // Event should be skipped
        verify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        verify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) } // Data query should not happen
    }

    @Test
    fun `Task processes events modified after last sync time`() = runTest {
        val lastSyncTimeMs = 10000L
        val eventTimeMs = lastSyncTimeMs + 1000L // 1 second after

        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns Timestamp(lastSyncTimeMs)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case_new"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(eventTimeMs)

        // Mock data cursor for the processed event
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Event should be processed
    }

    @Test
    fun `Task processes events modified milliseconds after last sync time`() = runTest {
        val lastSyncTimeMs = 10000L
        val eventTimeMs = lastSyncTimeMs + 1L // 1 millisecond after

        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns Timestamp(lastSyncTimeMs)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case_new"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(eventTimeMs)

        // Mock data cursor for the processed event
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Event should be processed
    }

    @Test
    fun `Task processes events modified exactly at last sync time`() = runTest {
        val lastSyncTimeMs = 10000L
        val eventTimeMs = lastSyncTimeMs // Exactly at last sync time

        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns Timestamp(lastSyncTimeMs)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case_new"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(eventTimeMs)

        // Mock data cursor for the processed event
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Event should be processed
    }
    
    @Test
    fun `Task processes events when last modified parse fails`() = runTest {
        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns Timestamp(10000L)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returns "case_bad_date"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns "Invalid Date String" // Will parse to 0L

        // Mock data cursor for the processed event (since parse fail -> 0L, which is not > 0 and not <= last sync)
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(0) } returns "subjectActions"
        every { mockDataCursor.getString(1) } returns SUBJECT_ACTIONS_EVENT_1
        
        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Event should be processed
        verify { Simber.e(any<String>(), ofType<Exception>(), tag = any<LoggingConstants.CrashReportTag>()) } // Verify date parsing error logged
    }

    @Test
    fun `Task processes all events when last sync time is null`() = runTest {
        coEvery { mockEventSyncCache.readLastSuccessfulSyncTime() } returns null // No last sync time

        val expectedCount = 2
        every { mockMetadataCursor.count } returns expectedCount
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockMetadataCursor.getString(mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID)) } returnsMany listOf("case1", "case2")
        val eventTimeMs = 10000L // Some valid time
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(eventTimeMs)

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.getString(0) } returnsMany listOf("subjectActions", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany listOf(SUBJECT_ACTIONS_EVENT_1, SUBJECT_ACTIONS_EVENT_2)

        val result = dataSource.getEvents()
        val events = result.eventFlow.toList()

        assertEquals(expectedCount, result.totalCount)
        assertEquals(expectedCount, events.size) // Event should be processed
    }

    @Test
    fun `exception during metadata cursor query is propagated`() = runTest {
        every {
            mockContentResolver.query(
                mockMetadataUri,
                arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED),
                any(),
                any(),
                any(),
            )
        } throws RuntimeException("Database error")

        val result = dataSource.getEvents()

        try {
            result.eventFlow.toList() // This will trigger the query
            assert(false) { "Expected RuntimeException" }
        } catch (e: RuntimeException) {
            assertEquals("Database error", e.message)
        }

        verify { Simber.e(any(), ofType<RuntimeException>()) }
        verify { mockContentResolver.query(
            mockMetadataUri,
            arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED),
            any(),
            any(),
            any()
        ) }
    }

    @Test
    fun `getPackageName uses preference value when available`() {
        val customPackageName = "custom.commcare.package"
        every { mockSharedPreferences.getString(any(), any()) } returns customPackageName

        val tempMockMetadataUri = mockk<Uri>(relaxed = true)
        every { Uri.parse("content://$customPackageName.case/casedb/case") } returns tempMockMetadataUri
        every { context.contentResolver.query(tempMockMetadataUri, null, null, null, null) } returns mockMetadataCursor
        every { mockMetadataCursor.count } returns 0 // For the count() call

        val result = dataSource.getEvents()

        assertEquals(0, result.totalCount)
        verify { mockSharedPreferences.getString(any(), any()) }
        // Verify that the query for count used the URI with the custom package name
        verify { context.contentResolver.query(tempMockMetadataUri, null, null, null, null) }
    }

    @Test
    fun `getPackageName falls back to default when preference is null`() {
        every { mockSharedPreferences.getString(any(), any()) } returns null

        val specificMockMetadataUriForThisTest = mockk<Uri>(relaxed = true)
        every { Uri.parse("content://$TEST_PACKAGE_NAME.case/casedb/case") } returns specificMockMetadataUriForThisTest
        every { context.contentResolver.query(
            specificMockMetadataUriForThisTest,
            null,
            null,
            null,
            null)
        } returns mockMetadataCursor

        // The fallback should use the default value passed to getString
        every { mockMetadataCursor.count } returns 0

        val result = dataSource.getEvents()

        assertEquals(0, result.totalCount)
        verify { mockSharedPreferences.getString(any(), any()) }
        // Verify that the query for count used the URI with the default package name
        verify { context.contentResolver.query(
            specificMockMetadataUriForThisTest,
            null,
            null,
            null,
            null
        ) }
    }
}
