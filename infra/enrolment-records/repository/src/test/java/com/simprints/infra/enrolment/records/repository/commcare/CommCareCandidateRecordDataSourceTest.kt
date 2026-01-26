package com.simprints.infra.enrolment.records.repository.commcare

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import com.simprints.core.domain.common.Modality
import com.simprints.core.domain.common.TemplateIdentifier.LEFT_INDEX_FINGER
import com.simprints.core.domain.common.TemplateIdentifier.LEFT_THUMB
import com.simprints.core.domain.reference.BiometricReference
import com.simprints.core.domain.reference.BiometricTemplate
import com.simprints.core.domain.reference.CandidateRecord
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.core.tools.utils.ExtractCommCareCaseIdUseCase
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.enrolment.records.repository.commcare.CommCareCandidateRecordDataSource.Companion.CASE_COUNT_FALLBACK_POLL_INTERVAL_MILLIS
import com.simprints.infra.enrolment.records.repository.commcare.CommCareCandidateRecordDataSource.Companion.COLUMN_DATUM_ID
import com.simprints.infra.enrolment.records.repository.commcare.CommCareCandidateRecordDataSource.Companion.COLUMN_VALUE
import com.simprints.infra.enrolment.records.repository.domain.models.BiometricDataSource
import com.simprints.infra.enrolment.records.repository.domain.models.EnrolmentRecordQuery
import com.simprints.infra.enrolment.records.repository.usecases.CompareImplicitTokenizedStringsUseCase
import com.simprints.infra.logging.Simber
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

class CommCareCandidateRecordDataSourceTest {
    companion object Companion {
        private const val SUBJECT_ACTIONS_FINGERPRINT_1 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"123","finger":"LEFT_THUMB"},{"quality":88,"template":"123","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FINGERPRINT_2 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"0e4b0028-be90-46f3-a5cb-5ea581478b24","templates":[{"quality":77,"template":"123","finger":"LEFT_THUMB"},{"quality":66,"template":"123","finger":"LEFT_INDEX_FINGER"}],"format":"NEC_1_5","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FACE_1 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"82902131-d753-447e-a600-6d3efa5df8b0","templates":[{"template":"123"}],"format":"ROC_1_23","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FACE_2 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"c7f4571f-2591-4c9d-bbd3-56dfdb82d206","templates":[{"template":"123"}],"format":"ROC_3","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_1 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"123","finger":"LEFT_THUMB"},{"quality":88,"template":"123","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"},{"id":"82902131-d753-447e-a600-6d3efa5df8b0","templates":[{"template":"123"}],"format":"ROC_1_23","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_2 =
            """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"0e4b0028-be90-46f3-a5cb-5ea581478b24","templates":[{"quality":77,"template":"123","finger":"LEFT_THUMB"},{"quality":66,"template":"123","finger":"LEFT_INDEX_FINGER"}],"format":"NEC_1_5","type":"FINGERPRINT_REFERENCE"},{"id":"c7f4571f-2591-4c9d-bbd3-56dfdb82d206","templates":[{"template":"123"}],"format":"ROC_3","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""

        private val expectedFingerprintCandidates = listOf(
            CandidateRecord(
                subjectId = "b26c91bc-b307-4131-80c3-55090ba5dbf2",
                references = listOf(
                    BiometricReference(
                        templates = listOf(
                            BiometricTemplate(
                                identifier = LEFT_THUMB,
                                template = byteArrayOf(),
                            ),
                            BiometricTemplate(
                                identifier = LEFT_INDEX_FINGER,
                                template = byteArrayOf(),
                            ),
                        ),
                        format = "ISO_19794_2",
                        referenceId = "2b9b4991-29d7-3eee-ac02-191afaa0c1a2",
                        modality = Modality.FINGERPRINT,
                    ),
                ),
            ),
            CandidateRecord(
                subjectId = "a961fcb4-8573-4270-a1b2-088e88275b00",
                references = listOf(
                    BiometricReference(
                        templates = listOf(
                            BiometricTemplate(
                                identifier = LEFT_THUMB,
                                template = byteArrayOf(),
                            ),
                            BiometricTemplate(
                                identifier = LEFT_INDEX_FINGER,
                                template = byteArrayOf(),
                            ),
                        ),
                        format = "NEC_1_5",
                        referenceId = "0e4b0028-be90-46f3-a5cb-5ea581478b24",
                        modality = Modality.FINGERPRINT,
                    ),
                ),
            ),
        )
        val expectedFaceCandidates = listOf(
            CandidateRecord(
                subjectId = "b26c91bc-b307-4131-80c3-55090ba5dbf2",
                references = listOf(
                    BiometricReference(
                        templates = listOf(
                            BiometricTemplate(
                                template = byteArrayOf(),
                            ),
                        ),
                        format = "ROC_1_23",
                        referenceId = "82902131-d753-447e-a600-6d3efa5df8b0",
                        modality = Modality.FACE,
                    ),
                ),
            ),
            CandidateRecord(
                subjectId = "a961fcb4-8573-4270-a1b2-088e88275b00",
                references = listOf(
                    BiometricReference(
                        templates = listOf(
                            BiometricTemplate(
                                template = byteArrayOf(),
                            ),
                        ),
                        format = "ROC_3",
                        referenceId = "c7f4571f-2591-4c9d-bbd3-56dfdb82d206",
                        modality = Modality.FACE,
                    ),
                ),
            ),
        )

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
            every { Uri.parse("content://org.commcare.dalvik.case/casedb/case") } returns mockMetadataUri
            every { Uri.parse("content://org.commcare.dalvik.case/casedb/data") } returns mockDataUri
            every { mockDataUri.buildUpon().appendPath(any()).build() } returns mockDataCaseIdUri
        }

        @JvmStatic
        @AfterClass
        fun cleanupClass() {
            clearAllMocks()
            unmockkAll()
            unmockkStatic(Uri::class)
        }
    }

    @MockK(relaxed = true)
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var encoder: EncodingUtils

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockContentResolver: ContentResolver

    @MockK
    private lateinit var useCase: CompareImplicitTokenizedStringsUseCase

    @MockK
    private lateinit var extractCommCareCaseIdUseCase: ExtractCommCareCaseIdUseCase

    private lateinit var mockMetadataCursor: Cursor

    private lateinit var mockDataCursor: Cursor

    private lateinit var dataSource: CommCareCandidateRecordDataSource

    @MockK
    lateinit var project: Project

    private val commCareBiometricDataSource = BiometricDataSource.CommCare("")

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { context.contentResolver } returns mockContentResolver

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

        every { encoder.base64ToBytes(any()) } returns byteArrayOf()
        every { useCase.invoke(any(), any(), any(), any()) } returns true
        every { extractCommCareCaseIdUseCase.invoke(any()) } returns null

        dataSource = CommCareCandidateRecordDataSource(
            timeHelper,
            encoder,
            useCase,
            extractCommCareCaseIdUseCase,
            4,
            context,
            testCoroutineRule.testCoroutineDispatcher,
        )
    }

    private fun setupMetadataCursor(
        count: Int,
        moveToNextResults: List<Boolean>,
    ) {
        every { mockMetadataCursor.count } returns count
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany moveToNextResults
    }

    private fun setupDataCursor(subjectActionsData: List<String>) {
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1

        val datumIds = subjectActionsData.flatMap { listOf("someOtherDatumId", "subjectActions") }
        every { mockDataCursor.getString(0) } returnsMany datumIds
        every { mockDataCursor.getString(1) } returnsMany subjectActionsData
    }

    private fun assertFingerprintCandidatesMatch(
        expected: List<CandidateRecord>,
        actual: List<CandidateRecord>,
        templateFormat: String,
    ) {
        val areContentsEqual = expected
            .filter { candidateRecord -> candidateRecord.references.any { it.format == templateFormat } }
            .zip(actual) { exp, act ->
                exp.subjectId == act.subjectId &&
                    exp.references
                        .zip(act.references) { expReference, actReference ->
                            expReference.templates == actReference.templates && expReference.format == actReference.format
                        }.all { it }
            }.all { it }
        assertTrue(areContentsEqual)
    }

    private fun assertFaceCandidatesMatch(
        expected: List<CandidateRecord>,
        actual: List<CandidateRecord>,
        templateFormat: String,
    ) {
        val areContentsEqual = expected
            .filter { candidateRecord -> candidateRecord.references.any { it.format == templateFormat } }
            .zip(actual) { exp, act ->
                exp.subjectId == act.subjectId &&
                    exp.references
                        .zip(act.references) { expReference, actReference ->
                            expReference.templates == actReference.templates && expReference.format == actReference.format
                        }.all { it }
            }.all { it }
        assertTrue(areContentsEqual)
    }

    @Test
    fun `test loadCandidateRecords with fingerprint records`() = runTest {
        setupMetadataCursor(expectedFingerprintCandidates.size, listOf(true, false))
        setupDataCursor(listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2))

        val templateFormat = "ISO_19794_2"
        val actualCandidates = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(format = templateFormat),
                ranges = listOf(0..expectedFingerprintCandidates.size),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualCandidates.addAll(it.identities)
            }

        val expected = expectedFingerprintCandidates
        assertEquals(1, actualCandidates.size)
        assertFingerprintCandidatesMatch(expected, actualCandidates, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadCandidateRecords with face records`() = runTest {
        setupMetadataCursor(expectedFaceCandidates.size, listOf(true, false))
        setupDataCursor(listOf(SUBJECT_ACTIONS_FACE_1, SUBJECT_ACTIONS_FACE_2))

        val templateFormat = "ROC_1_23"
        val actualCandidates = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(
                    format = templateFormat,
                    attendantId = TokenizableString.Tokenized("AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="),
                    moduleId = TokenizableString.Tokenized("AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="),
                    subjectId = "b26c91bc-b307-4131-80c3-55090ba5dbf2",
                ),
                ranges = listOf(0..expectedFaceCandidates.size),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualCandidates.addAll(it.identities)
            }

        assertEquals(1, actualCandidates.size)
        assertFaceCandidatesMatch(expectedFaceCandidates, actualCandidates, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadCandidateRecords returns only identities with fingerprint references`() = runTest {
        setupMetadataCursor(expectedFingerprintCandidates.size + 1, listOf(true, true, false))
        setupDataCursor(listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2, SUBJECT_ACTIONS_FACE_1))

        val templateFormat = "NEC_1_5"
        val actualIdentities = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(format = templateFormat),
                ranges = listOf(0..expectedFingerprintCandidates.size),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(1, actualIdentities.size)
        assertFingerprintCandidatesMatch(expectedFingerprintCandidates, actualIdentities, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadCandidateRecords returns only identities with face references`() = runTest {
        setupMetadataCursor(expectedFaceCandidates.size + 1, listOf(true, true, false))
        setupDataCursor(listOf(SUBJECT_ACTIONS_FACE_1, SUBJECT_ACTIONS_FACE_2, SUBJECT_ACTIONS_FINGERPRINT_1))

        val templateFormat = "ROC_1_23"
        val actualIdentities = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(format = templateFormat),
                ranges = listOf(0..expectedFaceCandidates.size),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(1, actualIdentities.size)
        assertFaceCandidatesMatch(expectedFaceCandidates, actualIdentities, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadCandidateRecords returns only fingerprint references for dual modality identities`() = runTest {
        setupMetadataCursor(expectedFingerprintCandidates.size, listOf(true, false))
        setupDataCursor(listOf(SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_1, SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_2))

        val templateFormat = "ISO_19794_2"
        val actualIdentities = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(format = templateFormat),
                ranges = listOf(0..expectedFingerprintCandidates.size),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(1, actualIdentities.size)
        assertFingerprintCandidatesMatch(expectedFingerprintCandidates, actualIdentities, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadCandidateRecords returns only face references for dual modality identities`() = runTest {
        setupMetadataCursor(expectedFaceCandidates.size, listOf(true, false))
        setupDataCursor(listOf(SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_1, SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_2))

        val templateFormat = "ROC_1_23"
        val actualIdentities = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(format = templateFormat),
                ranges = listOf(0..expectedFaceCandidates.size),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(1, actualIdentities.size)
        assertFaceCandidatesMatch(expectedFaceCandidates, actualIdentities, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun testCount() = runTest {
        val expectedCount = 5
        every { mockMetadataCursor.count } returns expectedCount

        val query = EnrolmentRecordQuery()
        val actualCount = dataSource.count(query)

        assertEquals(expectedCount, actualCount)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockMetadataCursor.count }
    }

    @Test
    fun `observeCount emits an initial 0 if no records`() = runTest {
        val contentObserver = slot<ContentObserver>()
        every { mockMetadataCursor.count } returns 0
        every { mockContentResolver.registerContentObserver(mockMetadataUri, true, capture(contentObserver)) } just Runs
        every { mockContentResolver.unregisterContentObserver(any()) } just Runs
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            dataSource
                .observeCount(EnrolmentRecordQuery(), commCareBiometricDataSource)
                .collect { channel.trySend(it) }
        }

        val firstEmission = channel.receive()
        collectJob.cancel()
        assertEquals(0, firstEmission)
    }

    @Test
    fun `observeCount emits updated count after content observer invalidation`() = runTest {
        val contentObserver = slot<ContentObserver>()
        var metadataCount = 0
        every { mockMetadataCursor.count } answers { metadataCount }
        every { mockContentResolver.registerContentObserver(mockMetadataUri, true, capture(contentObserver)) } just Runs
        every { mockContentResolver.unregisterContentObserver(any()) } just Runs
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            dataSource
                .observeCount(EnrolmentRecordQuery(), commCareBiometricDataSource)
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()
        metadataCount = 1
        contentObserver.captured.onChange(false)

        var updated: Int
        do {
            updated = channel.receive()
        } while (updated != 1)
        collectJob.cancel()
        assertEquals(0, initial)
        assertEquals(1, updated)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `observeCount polls periodically as a fallback`() = runTest {
        val contentObserver = slot<ContentObserver>()
        var metadataCount = 0
        every { mockMetadataCursor.count } answers { metadataCount }
        every { mockContentResolver.registerContentObserver(mockMetadataUri, true, capture(contentObserver)) } just Runs
        every { mockContentResolver.unregisterContentObserver(any()) } just Runs
        val channel = Channel<Int>(Channel.UNLIMITED)

        val collectJob = launch {
            dataSource
                .observeCount(EnrolmentRecordQuery(), commCareBiometricDataSource)
                .collect { channel.trySend(it) }
        }

        val initial = channel.receive()
        metadataCount = 2
        advanceTimeBy(CASE_COUNT_FALLBACK_POLL_INTERVAL_MILLIS)
        runCurrent()

        var polled: Int
        do {
            polled = channel.receive()
        } while (polled != 2)
        collectJob.cancel()
        assertEquals(0, initial)
        assertEquals(2, polled)
    }

    @Test
    fun `test handling of null metadata cursor`() = runTest {
        every {
            mockContentResolver.query(
                mockMetadataUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns null

        val query = EnrolmentRecordQuery()
        val range = 0..0
        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = query,
                ranges = listOf(range),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertTrue(actualIdentities.isEmpty())
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test metadata cursor size below range's first`() = runTest {
        every { mockMetadataCursor.count } returns 1

        val query = EnrolmentRecordQuery()
        val range = 2..3
        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = query,
                ranges = listOf(range),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertTrue(actualIdentities.isEmpty())
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test metadata cursor size bigger than range`() = runTest {
        setupMetadataCursor(expectedFingerprintCandidates.size + 1, listOf(true, true))
        every { mockMetadataCursor.position } returnsMany listOf(1, 2)
        setupDataCursor(listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2))

        val templateFormat = "ISO_19794_2"
        val actualIdentities = mutableListOf<CandidateRecord>()

        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(format = templateFormat),
                ranges = listOf(expectedFingerprintCandidates.indices),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(1, actualIdentities.size)
        assertFingerprintCandidatesMatch(expectedFingerprintCandidates, actualIdentities, templateFormat)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `empty caseId results in empty result`() = runTest {
        setupMetadataCursor(2, listOf(true, false))
        every { mockDataCursor.moveToNext() } returns true
        every { mockMetadataCursor.getString(any()) } returns null

        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(),
                ranges = listOf(0..2),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `exception during metadata cursor access is reported`() = runTest {
        every { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) } throws RuntimeException("Some exception")

        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(),
                ranges = listOf(0..2),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
        coVerify { Simber.e(any(), ofType<RuntimeException>()) }
    }

    @Test
    fun `data cursor is null`() = runTest {
        setupMetadataCursor(2, listOf(true, false))
        every { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) } returns null

        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(),
                ranges = listOf(0..2),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `subjectActions not found in cursor data`() = runTest {
        setupMetadataCursor(2, listOf(true, false))
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, true, false)
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returns "someKey"
        every { mockDataCursor.getString(1) } returns "someValue"

        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(),
                ranges = listOf(0..2),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `subjectActions contains invalid JSON`() = runTest {
        setupMetadataCursor(2, listOf(true, false))
        setupDataCursor(listOf("invalid JSON 1", "invalid JSON 2"))

        val actualIdentities = mutableListOf<CandidateRecord>()
        dataSource
            .loadCandidateRecords(
                query = EnrolmentRecordQuery(),
                ranges = listOf(0..2),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = {},
            ).consumeEach {
                actualIdentities.addAll(it.identities)
            }

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
        coVerify { Simber.e(any(), ofType<Exception>()) }
    }

    @Test
    fun `null metadata cursor during count`() = runTest {
        every {
            mockContentResolver.query(
                mockMetadataUri,
                any(),
                any(),
                any(),
                any(),
            )
        } returns null

        val query = EnrolmentRecordQuery()
        val actualCount = dataSource.count(query)

        assertEquals(0, actualCount)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `count with case ID in metadata returns 1 without database query`() = runTest {
        val testCaseId = "test-case-id"
        every { extractCommCareCaseIdUseCase.invoke(any()) } returns testCaseId

        val query = EnrolmentRecordQuery(metadata = "test-metadata")
        val actualCount = dataSource.count(query, commCareBiometricDataSource)

        assertEquals(1, actualCount)
        // Verify that no ContentResolver query was made since we have a case ID
        coVerify(exactly = 0) { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `count without case ID in metadata queries database`() = runTest {
        val expectedCount = 3
        every { extractCommCareCaseIdUseCase.invoke(any()) } returns null
        every { mockMetadataCursor.count } returns expectedCount

        val query = EnrolmentRecordQuery(metadata = "test-metadata")
        val actualCount = dataSource.count(query, commCareBiometricDataSource)

        assertEquals(expectedCount, actualCount)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }

    @Test
    fun `loadCandidateRecords with case ID calls onCandidateLoaded`() = runTest {
        val testCaseId = "test-case-id"
        var onCandidateLoadedCalled = false
        every { extractCommCareCaseIdUseCase.invoke(any()) } returns testCaseId
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany listOf("someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returns SUBJECT_ACTIONS_FINGERPRINT_1

        val query = EnrolmentRecordQuery(metadata = "test-metadata")
        dataSource
            .loadCandidateRecords(
                query = query,
                ranges = listOf(0..1),
                project = project,
                dataSource = commCareBiometricDataSource,
                scope = this,
                onCandidateLoaded = { onCandidateLoadedCalled = true },
            ).consumeEach { }

        assertTrue(onCandidateLoadedCalled)
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
        // Verify that metadata query was not made since we have a case ID
        coVerify(exactly = 0) { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }
}
