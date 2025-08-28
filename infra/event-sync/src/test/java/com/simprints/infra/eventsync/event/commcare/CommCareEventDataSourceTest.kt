package com.simprints.infra.eventsync.event.commcare

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.LastCallingPackageStore
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordDeletionEvent
import com.simprints.infra.events.sampledata.SampleDefaults.DEFAULT_PROJECT_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.BATCH_SIZE
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_CASE_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_DATUM_ID
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_LAST_MODIFIED
import com.simprints.infra.eventsync.event.commcare.CommCareEventDataSource.Companion.COLUMN_VALUE
import com.simprints.infra.eventsync.event.commcare.cache.CommCareSyncCache
import com.simprints.infra.eventsync.event.commcare.cache.SyncedCaseEntity
import com.simprints.infra.eventsync.status.down.domain.RemoteEventQuery
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.libsimprints.Constants.SIMPRINTS_COSYNC_SUBJECT_ACTIONS
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
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
        private const val SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID = "b26c91bc-b307-4131-80c3-55090ba5dbf2"
        private const val SUBJECT_ACTIONS_EVENT_1 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"$SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"123","finger":"LEFT_THUMB"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_EVENT_2_SUBJECT_ID = "a961fcb4-8573-4270-a1b2-088e88275b00"
        private const val SUBJECT_ACTIONS_EVENT_2 =
            """{"events":[{"id":"1eafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"$SUBJECT_ACTIONS_EVENT_2_SUBJECT_ID","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":88,"template":"456","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val INVALID_JSON = """{"invalid": json"""

        private const val COLUMN_INDEX_DATUM_ID = 0
        private const val COLUMN_INDEX_VALUE = 1
        private const val COLUMN_INDEX_CASE_ID = 2
        private const val COLUMN_INDEX_LAST_MODIFIED = 3

        private val emptyEventQuery = RemoteEventQuery(projectId = DEFAULT_PROJECT_ID, modes = emptyList())

        // Helper to format date strings as CommCare does (using US locale to match Java's Date.toString())
        private val commCareDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)

        private fun formatCommCareDate(millis: Long): String = commCareDateFormat.format(Date(millis))

        @JvmStatic
        lateinit var mockMetadataUri: Uri

        @JvmStatic
        lateinit var mockDataUri: Uri

        @JvmStatic
        lateinit var mockDataCaseIdUri: Uri

        @JvmStatic
        lateinit var mockMetadataCaseIdUri: Uri

        @JvmStatic
        @BeforeClass
        fun setupClass() {
            mockkObject(Simber)
            mockMetadataUri = mockk(relaxed = true)
            mockDataUri = mockk(relaxed = true)
            mockDataCaseIdUri = mockk(relaxed = true)
            mockMetadataCaseIdUri = mockk(relaxed = true)
            mockkStatic(Uri::class)
            every { Uri.parse("content://$TEST_PACKAGE_NAME.case/casedb/case") } returns mockMetadataUri
            every { Uri.parse("content://$TEST_PACKAGE_NAME.case/casedb/data") } returns mockDataUri
            every { mockDataUri.buildUpon().appendPath(any()).build() } returns mockDataCaseIdUri
            every { mockMetadataUri.buildUpon().appendPath(any()).build() } returns mockMetadataCaseIdUri
        }

        @JvmStatic
        @AfterClass
        fun cleanupClass() {
            clearAllMocks()
            unmockkAll()
            unmockkStatic(Uri::class)
        }
    }

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockContentResolver: ContentResolver

    @MockK
    private lateinit var mockLastCallingPackageStore: LastCallingPackageStore

    @MockK(relaxUnitFun = true)
    private lateinit var mockCommCareSyncCache: CommCareSyncCache

    private lateinit var mockMetadataCursor: Cursor
    private lateinit var mockDataCursor: Cursor
    private lateinit var dataSource: CommCareEventDataSource

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver } returns mockContentResolver
        every { context.getString(any()) } returns TEST_PACKAGE_NAME
        every { mockLastCallingPackageStore.lastCallingPackageName } returns TEST_PACKAGE_NAME

        every { Uri.parse(any()) } answers {
            val uriPath = it.invocation.args[0] as String
            if (uriPath.endsWith("case")) mockMetadataUri else mockDataUri
        }

        mockMetadataCursor = mockk(relaxed = true)
        mockDataCursor = mockk(relaxed = true)

        every { mockMetadataCursor.close() } just Runs
        every { mockDataCursor.close() } just Runs

        // Default behavior for column indices
        every { mockMetadataCursor.getColumnIndexOrThrow(COLUMN_CASE_ID) } returns COLUMN_INDEX_CASE_ID
        every { mockMetadataCursor.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED) } returns COLUMN_INDEX_LAST_MODIFIED
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns COLUMN_INDEX_DATUM_ID
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns COLUMN_INDEX_VALUE

        // Default behavior for last modified time
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(10000L)

        // Default behaviour for previously synced cases: none
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns emptyList()

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

        every {
            mockContentResolver.query(
                mockMetadataCaseIdUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns mockMetadataCursor

        dataSource = CommCareEventDataSource(
            JsonHelper,
            mockCommCareSyncCache,
            mockLastCallingPackageStore,
            context,
        )
    }

    @Test
    fun `getEvents returns correct count and event flow`() = runTest {
        val caseId1 = "case1"
        val caseId2 = "case2"
        val expectedCount = 2
        every { mockMetadataCursor.count } returns expectedCount
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returnsMany listOf(caseId1, caseId2)

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, false) // datum_id, value for each case
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returnsMany listOf(SUBJECT_ACTIONS_EVENT_1, SUBJECT_ACTIONS_EVENT_2)

        val result = dataSource.getEvents(emptyEventQuery)

        assertEquals(expectedCount, result.totalCount)
        val events = result.eventFlow.toList()
        assertEquals(2, events.size)
        assertTrue(events[0] is EnrolmentRecordCreationEvent)
        val event1 = events[0] as EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID, event1.payload.subjectId)

        assertTrue(events[1] is EnrolmentRecordCreationEvent)
        val event2 = events[1] as EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_2_SUBJECT_ID, event2.payload.subjectId)

        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 2) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `getEvents generates deletion event for case not in CommCare`() = runTest {
        val caseIdPresent = "case_present"
        val caseIdMissing = "case_missing_to_delete"
        val simprintsIdForMissingCase = "simprints_id_for_missing_case"

        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseIdPresent

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        // Setup previously synced cases
        val previouslySyncedCases = listOf(
            SyncedCaseEntity(caseIdPresent, "some_sid", 5000L),
            SyncedCaseEntity(caseIdMissing, simprintsIdForMissingCase, 5000L),
        )
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns previouslySyncedCases

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(2, events.size) // One creation for present, one deletion for missing

        val creationEvent = events.find { it is EnrolmentRecordCreationEvent } as? EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID, creationEvent?.payload?.subjectId)

        val deletionEvent = events.find { it is EnrolmentRecordDeletionEvent } as? EnrolmentRecordDeletionEvent
        assertEquals(simprintsIdForMissingCase, deletionEvent?.payload?.subjectId)
    }

    @Test
    fun `getEvents does not generate deletion events when CommCare response is empty`() = runTest {
        val caseIdMissing = "case_missing_but_commcare_empty"
        val simprintsIdForMissingCase = "simprints_id_for_missing_empty"

        every { mockMetadataCursor.count } returns 0
        every { mockMetadataCursor.moveToNext() } returns false

        // Setup previously synced cases
        val previouslySyncedCases = listOf(
            SyncedCaseEntity(caseIdMissing, simprintsIdForMissingCase, 5000L),
        )
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns previouslySyncedCases

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(0, result.totalCount)
        assertEquals(0, events.size) // No deletion events because CommCare was empty
    }

    @Test
    fun `getEvents skips case when lastModified is not newer than lastSyncedTimestamp`() = runTest {
        val caseId = "case1"
        val lastSyncedTimestamp = 15000L
        val commCareLastModified = 10000L // Older than lastSyncedTimestamp

        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseId
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(commCareLastModified)

        // Setup previously synced case
        val previouslySyncedCases = listOf(
            SyncedCaseEntity(caseId, "some_sid", lastSyncedTimestamp),
        )
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns previouslySyncedCases

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // Case skipped because not modified since last sync

        verify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `getEvents processes case when lastModified is newer than lastSyncedTimestamp`() = runTest {
        val caseId = "case1"
        val lastSyncedTimestamp = 5000L
        val commCareLastModified = 15000L // Newer than lastSyncedTimestamp

        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseId
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(commCareLastModified)

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        // Setup previously synced case
        val previouslySyncedCases = listOf(
            SyncedCaseEntity(caseId, "some_sid", lastSyncedTimestamp),
        )
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns previouslySyncedCases

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Case processed because modified since last sync

        verify(exactly = 1) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `count returns correct value when cursor is available`() {
        val expectedCount = 5
        every { mockMetadataCursor.count } returns expectedCount

        val result = dataSource.getEvents(emptyEventQuery) // count is called internally

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

        val result = dataSource.getEvents(emptyEventQuery)

        assertEquals(0, result.totalCount)
        verify { mockContentResolver.query(mockMetadataUri, null, null, null, null) }
    }

    @Test
    fun `syncAllUpdatedCases handles empty case list`() = runTest {
        every { mockMetadataCursor.count } returns 0
        every { mockMetadataCursor.moveToNext() } returns false

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(0, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
    }

    @Test
    fun `syncAllUpdatedCases handles null case id`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns null

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // No caseId, so no events processed
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
    }

    @Test
    fun `syncAllUpdatedCases handles empty case id`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns ""

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // Empty caseId, so no events processed
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
    }

    @Test
    fun `loadDataFromCommCare throws exception when data cursor is null`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"
        every {
            mockContentResolver.query(
                mockDataCaseIdUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns null

        val result = dataSource.getEvents(emptyEventQuery)

        try {
            result.eventFlow.toList()
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("Cursor for caseId case1 is null", e.message)
        }

        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `getSubjectActionsValue returns empty string when subjectActions not found`() = runTest {
        val caseId = "case1"
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseId

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returnsMany listOf("someOtherKey", "anotherKey")
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returnsMany listOf("someValue", "anotherValue")

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `parseRecordEvents handles invalid JSON gracefully`() = runTest {
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns INVALID_JSON

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // Event parsing fails, so no event emitted
        verify { Simber.e(any(), ofType<Exception>()) }
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `parseRecordEvents handles empty subjectActions`() = runTest {
        val caseId = "case1"
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseId

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns ""

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size)
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `syncAllUpdatedCases processes events in batches`() = runTest {
        val caseCount = BATCH_SIZE + 5
        every { mockMetadataCursor.count } returns caseCount

        val moveNextResultsMetadata = (1..caseCount).map { true } + false
        every { mockMetadataCursor.moveToNext() } returnsMany moveNextResultsMetadata
        val caseIds = (1..caseCount).map { "case$it" }
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returnsMany caseIds

        // Mock data cursor for each case, assuming one subjectActions per case
        val moveNextResultsData = (1..caseCount).map { true } + List(caseCount) { false } // true then false for each caseId
        every { mockDataCursor.moveToNext() } returnsMany moveNextResultsData
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        val subjectActionsData = (1..caseCount).map { SUBJECT_ACTIONS_EVENT_1 }
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returnsMany subjectActionsData

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(caseCount, result.totalCount)
        assertEquals(caseCount, events.size)
        assertTrue(events.all { it is EnrolmentRecordCreationEvent })

        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = caseCount) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `parseCommCareDateToMillis handles valid date format`() = runTest {
        val validDate = formatCommCareDate(12345L)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns validDate

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size)
    }

    @Test
    fun `parseCommCareDateToMillis handles numeric timezone format as fallback`() = runTest {
        // Test fallback format with numeric timezone (Z pattern)
        val numericTimezoneDate = "Mon Oct 05 16:17:01 -0400 2015"
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns numericTimezoneDate

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Successfully parsed with fallback format
        // Should log error for first format attempt but succeed with second
        verify { Simber.e(any(), ofType<Exception>(), tag = LoggingConstants.CrashReportTag.COMMCARE_SYNC) }
    }

    @Test
    fun `parseCommCareDateToMillis handles invalid date format`() = runTest {
        val invalidDate = "invalid date format"
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns invalidDate

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Still processes because invalid date defaults to 0L
        // Should log error for each format attempt plus final failure warning
        verify(atLeast = 2) { Simber.e(any(), ofType<Exception>(), tag = LoggingConstants.CrashReportTag.COMMCARE_SYNC) }
        verify { Simber.w(any(), tag = LoggingConstants.CrashReportTag.COMMCARE_SYNC) }
    }

    @Test
    fun `onEventsProcessed updates cache for creation events`() = runTest {
        val caseId = "case1"
        val subjectId = SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID
        val lastModifiedTime = 15000L

        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseId
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(lastModifiedTime)

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        // Call onEventsProcessed with the collected events
        dataSource.onEventsProcessed(events)

        // Verify that the cache was updated with the correct SyncedCaseEntity
        coVerify {
            mockCommCareSyncCache.addSyncedCase(
                match<SyncedCaseEntity> {
                    it.caseId == caseId &&
                        it.simprintsId == subjectId &&
                        it.lastSyncedTimestamp == lastModifiedTime
                },
            )
        }
    }

    @Test
    fun `onEventsProcessed removes cache for deletion events`() = runTest {
        val caseIdPresent = "case_present"
        val caseIdMissing = "case_missing"
        val simprintsIdForMissingCase = "simprints_id_for_missing_case"

        // Setup scenario where CommCare has some cases (not empty)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseIdPresent

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        // Setup previously synced cases - one present in CommCare, one missing
        val previouslySyncedCases = listOf(
            SyncedCaseEntity(caseIdPresent, "some_sid", 5000L),
            SyncedCaseEntity(caseIdMissing, simprintsIdForMissingCase, 5000L),
        )
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns previouslySyncedCases

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        // Should have one creation event for present case and one deletion event for missing case
        assertEquals(1, result.totalCount)
        assertEquals(2, events.size)

        val creationEvent = events.find { it is EnrolmentRecordCreationEvent } as? EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID, creationEvent?.payload?.subjectId)

        val deletionEvent = events.find { it is EnrolmentRecordDeletionEvent } as? EnrolmentRecordDeletionEvent
        assertEquals(simprintsIdForMissingCase, deletionEvent?.payload?.subjectId)

        // Call onEventsProcessed with the collected events
        dataSource.onEventsProcessed(events)

        // Verify that the case was removed from cache for the deletion event
        coVerify { mockCommCareSyncCache.removeSyncedCase(caseIdMissing) }
        // Verify that the present case was added/updated in cache for the creation event
        coVerify {
            mockCommCareSyncCache.addSyncedCase(
                match<SyncedCaseEntity> {
                    it.caseId == caseIdPresent &&
                        it.simprintsId == SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID
                },
            )
        }
    }

    @Test
    fun `generateEnrolmentRecordDeletionEvent skips deletion event and removes cache for empty simprintsId`() = runTest {
        val caseIdPresent = "case_present"
        val caseIdMissingWithEmptySimprints = "case_missing_empty_simprints"

        // Setup scenario where CommCare has some cases (not empty)
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseIdPresent

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        // Setup previously synced cases - one present in CommCare, one missing with empty simprintsId
        val previouslySyncedCases = listOf(
            SyncedCaseEntity(caseIdPresent, "some_sid", 5000L),
            SyncedCaseEntity(caseIdMissingWithEmptySimprints, "", 5000L), // Empty simprintsId
        )
        coEvery { mockCommCareSyncCache.getAllSyncedCases() } returns previouslySyncedCases

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        // Should have one creation event for present case, no deletion event for missing case with empty simprintsId
        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Only creation event, no deletion event

        val creationEvent = events.find { it is EnrolmentRecordCreationEvent } as? EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID, creationEvent?.payload?.subjectId)

        // Verify no deletion events were generated
        val deletionEvents = events.filterIsInstance<EnrolmentRecordDeletionEvent>()
        assertEquals(0, deletionEvents.size)

        // Verify that the case with empty simprintsId was removed from cache directly
        coVerify { mockCommCareSyncCache.removeSyncedCase(caseIdMissingWithEmptySimprints) }
    }

    @Test
    fun `loadEnrolmentRecordCreationEvents adds case to cache with empty simprintsId when no valid enrolment records found`() = runTest {
        val caseId = "case1"
        val lastModifiedTime = 15000L

        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns caseId
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(lastModifiedTime)

        // Setup cursor to return invalid/null JSON that results in null coSyncEnrolmentRecordEvents
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns "" // This will cause null parsing

        val result = dataSource.getEvents(emptyEventQuery)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // No events because invalid JSON resulted in null parsing

        // Verify that case was added to cache with empty simprintsId
        coVerify {
            mockCommCareSyncCache.addSyncedCase(
                match<SyncedCaseEntity> {
                    it.caseId == caseId &&
                        it.simprintsId == "" &&
                        it.lastSyncedTimestamp == lastModifiedTime
                },
            )
        }
    }

    @Test
    fun `getEvents with externalIds syncs specific cases only`() = runTest {
        val specificCaseIds = listOf("case123", "case456")
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = specificCaseIds,
        )

        // Create separate cursors for each case to simulate individual queries
        val mockMetadataCursor1 = mockk<Cursor>(relaxed = true)
        val mockMetadataCursor2 = mockk<Cursor>(relaxed = true)
        val mockDataCursor1 = mockk<Cursor>(relaxed = true)
        val mockDataCursor2 = mockk<Cursor>(relaxed = true)

        // Mock metadata cursors for individual case queries
        every { mockMetadataCursor1.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor1.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(15000L)
        every { mockMetadataCursor1.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED) } returns COLUMN_INDEX_LAST_MODIFIED
        every { mockMetadataCursor1.close() } just Runs

        every { mockMetadataCursor2.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor2.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(20000L)
        every { mockMetadataCursor2.getColumnIndexOrThrow(COLUMN_LAST_MODIFIED) } returns COLUMN_INDEX_LAST_MODIFIED
        every { mockMetadataCursor2.close() } just Runs

        // Mock data cursors for individual case queries
        every { mockDataCursor1.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor1.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor1.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1
        every { mockDataCursor1.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns COLUMN_INDEX_DATUM_ID
        every { mockDataCursor1.getColumnIndexOrThrow(COLUMN_VALUE) } returns COLUMN_INDEX_VALUE
        every { mockDataCursor1.close() } just Runs

        every { mockDataCursor2.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor2.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor2.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_2
        every { mockDataCursor2.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns COLUMN_INDEX_DATUM_ID
        every { mockDataCursor2.getColumnIndexOrThrow(COLUMN_VALUE) } returns COLUMN_INDEX_VALUE
        every { mockDataCursor2.close() } just Runs

        // Override content resolver to return different cursors for different queries
        every {
            mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any())
        } returnsMany listOf(mockMetadataCursor1, mockMetadataCursor2)

        every {
            mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any())
        } returnsMany listOf(mockDataCursor1, mockDataCursor2)

        val result = dataSource.getEvents(queryWithExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(specificCaseIds.size, result.totalCount)
        assertEquals(2, events.size)
        assertTrue(events.all { it is EnrolmentRecordCreationEvent })

        // Verify specific case metadata queries were made
        verify(
            exactly = 2,
        ) { mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 2) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }

        // Verify general metadata query was NOT made (only for all cases sync)
        verify(
            exactly = 0,
        ) { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
    }

    @Test
    fun `getEvents with single externalId processes successfully`() = runTest {
        val singleCaseId = "case789"
        val queryWithSingleId = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(singleCaseId),
        )

        // Mock metadata cursor for the single case
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(12000L)

        // Mock data cursor for subjectActions
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithSingleId)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size)
        assertTrue(events[0] is EnrolmentRecordCreationEvent)

        val event = events[0] as EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID, event.payload.subjectId)

        verify(
            exactly = 1,
        ) { mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 1) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `getEvents with empty externalIds list syncs all cases`() = runTest {
        val queryWithEmptyExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = emptyList(),
        )

        // Mock general case sync behavior
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithEmptyExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size)

        // Should use syncAllUpdatedCases path (general metadata query)
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 0) { mockContentResolver.query(mockMetadataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `getEvents with null externalIds list syncs all cases`() = runTest {
        val queryWithEmptyExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = null,
        )

        // Mock general case sync behavior
        every { mockMetadataCursor.count } returns 1
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_CASE_ID) } returns "case1"

        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithEmptyExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size)

        // Should use syncAllUpdatedCases path (general metadata query)
        verify { mockContentResolver.query(mockMetadataUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 0) { mockContentResolver.query(mockMetadataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `syncSpecificCases handles case with no lastModified data`() = runTest {
        val caseId = "case_no_metadata"
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(caseId),
        )

        // Mock metadata cursor returning no data (empty result set)
        every { mockMetadataCursor.moveToNext() } returns false

        // Mock data cursor for subjectActions (still should get called)
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount) // Count is based on externalIds size
        assertEquals(1, events.size) // Event still created with default timestamp (0L)

        verify(
            exactly = 1,
        ) { mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 1) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `syncSpecificCases handles case with multiple lastModified entries`() = runTest {
        val caseId = "case_multiple_metadata"
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(caseId),
        )

        // Mock metadata cursor returning multiple entries (should use last one)
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returnsMany listOf(
            formatCommCareDate(10000L),
            formatCommCareDate(20000L),
        )

        // Mock data cursor for subjectActions
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size)

        verify(
            exactly = 1,
        ) { mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 1) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `syncSpecificCases handles case with invalid lastModified date`() = runTest {
        val caseId = "case_invalid_date"
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(caseId),
        )

        // Mock metadata cursor with invalid date
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns "invalid date string"

        // Mock data cursor for subjectActions
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size) // Event created with 0L timestamp due to invalid date

        // Should log date parsing errors
        verify(atLeast = 2) { Simber.e(any(), ofType<Exception>(), tag = LoggingConstants.CrashReportTag.COMMCARE_SYNC) }
        verify { Simber.w(any(), tag = LoggingConstants.CrashReportTag.COMMCARE_SYNC) }
    }

    @Test
    fun `syncSpecificCases throws exception when data cursor is null for specific case`() = runTest {
        val caseId = "case_null_data_cursor"
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(caseId),
        )

        // Mock metadata cursor
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(15000L)

        // Mock null data cursor for the specific case
        every {
            mockContentResolver.query(
                mockDataCaseIdUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns null

        val result = dataSource.getEvents(queryWithExternalIds)

        try {
            result.eventFlow.toList()
            assert(false) { "Expected IllegalStateException" }
        } catch (e: IllegalStateException) {
            assertEquals("Cursor for caseId $caseId is null", e.message)
        }

        verify(
            exactly = 1,
        ) { mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 1) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `syncSpecificCases handles case with no subjectActions data`() = runTest {
        val caseId = "case_no_subject_actions"
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(caseId),
        )

        // Mock metadata cursor
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(15000L)

        // Mock data cursor with no subjectActions
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returnsMany listOf("other_key", "another_key")
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returnsMany listOf("other_value", "another_value")

        val result = dataSource.getEvents(queryWithExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(0, events.size) // No events because no subjectActions found

        verify(
            exactly = 1,
        ) { mockContentResolver.query(mockMetadataCaseIdUri, arrayOf(COLUMN_CASE_ID, COLUMN_LAST_MODIFIED), any(), any(), any()) }
        verify(exactly = 1) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `syncSpecificCases adds processed cases to pendingSyncedCases correctly`() = runTest {
        val caseId = "test_case_pending"
        val queryWithExternalIds = RemoteEventQuery(
            projectId = DEFAULT_PROJECT_ID,
            modes = emptyList(),
            externalIds = listOf(caseId),
        )
        val expectedLastModifiedTime = 18000L

        // Mock metadata cursor
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockMetadataCursor.getString(COLUMN_INDEX_LAST_MODIFIED) } returns formatCommCareDate(expectedLastModifiedTime)

        // Mock data cursor for subjectActions
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.getString(COLUMN_INDEX_DATUM_ID) } returns SIMPRINTS_COSYNC_SUBJECT_ACTIONS
        every { mockDataCursor.getString(COLUMN_INDEX_VALUE) } returns SUBJECT_ACTIONS_EVENT_1

        val result = dataSource.getEvents(queryWithExternalIds)
        val events = result.eventFlow.toList()

        assertEquals(1, result.totalCount)
        assertEquals(1, events.size)

        val event = events[0] as EnrolmentRecordCreationEvent
        assertEquals(SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID, event.payload.subjectId)

        // Call onEventsProcessed to verify pendingSyncedCases was populated correctly
        dataSource.onEventsProcessed(events)

        // Verify that the case was added to cache with correct data
        coVerify {
            mockCommCareSyncCache.addSyncedCase(
                match<SyncedCaseEntity> {
                    it.caseId == caseId &&
                        it.simprintsId == SUBJECT_ACTIONS_EVENT_1_SUBJECT_ID &&
                        it.lastSyncedTimestamp == expectedLastModifiedTime
                },
            )
        }
    }
}
