package com.simprints.infra.enrolment.records.store.commcare

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.IFingerIdentifier.LEFT_INDEX_FINGER
import com.simprints.core.domain.fingerprint.IFingerIdentifier.LEFT_THUMB
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.enrolment.records.store.commcare.CommCareIdentityDataSource.Companion.COLUMN_DATUM_ID
import com.simprints.infra.enrolment.records.store.commcare.CommCareIdentityDataSource.Companion.COLUMN_VALUE
import com.simprints.infra.enrolment.records.store.domain.models.FaceIdentity
import com.simprints.infra.enrolment.records.store.domain.models.FingerprintIdentity
import com.simprints.infra.enrolment.records.store.domain.models.SubjectQuery
import com.simprints.infra.logging.Simber
import io.mockk.*
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class CommCareIdentityDataSourceTest {

    companion object {
        private const val SUBJECT_ACTIONS_FINGERPRINT_1 = """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA","finger":"LEFT_THUMB"},{"quality":88,"template":"Rk1SACAyMAAAAAEUAAABLAGQAMUAxQEAABBYKUBqACiHAIBKAD0RAECyAEjuAEAfAF6sAEAnAG4sAIB4AHT2AIByAIuiAEAGAIs1AEC9AJDUAEB3AJS9AICaAJXRAEDzAJ/bAECHAKi9AIBzALJDAEAXALfDAEB6ALszAIDGAL3JAED2AL/QAEBHAMrRAEENAMrWAECiAMy9AIAkANfNAEDwANnKAEB3AOr4AICjAPKhAEANAPXGAIEEAPjJAEBCAPzaAICNAQUDAEEZAQdpAEDOARsfAIDpAR0TAEDZASUUAEDnASeNAIDsAS0NAED+ATONAEDxAT8KAIB9AUB5AEDAAVUNAEBdAVZzAECCAWt9AAAA","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FINGERPRINT_2 = """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"ePZ3yiWTt9etQAWu+A3H0WGtHI2uod=="},"attendantId":{"value":"r9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzS="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":77,"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA","finger":"LEFT_THUMB"},{"quality":66,"template":"Rk1SACAyMAAAAAEUAAABLAGQAMUAxQEAABBYKUBqACiHAIBKAD0RAECyAEjuAEAfAF6sAEAnAG4sAIB4AHT2AIByAIuiAEAGAIs1AEC9AJDUAEB3AJS9AICaAJXRAEDzAJ/bAECHAKi9AIBzALJDAEAXALfDAEB6ALszAIDGAL3JAED2AL/QAEBHAMrRAEENAMrWAECiAMy9AIAkANfNAEDwANnKAEB3AOr4AICjAPKhAEANAPXGAIEEAPjJAEBCAPzaAICNAQUDAEEZAQdpAEDOARsfAIDpAR0TAEDZASUUAEDnASeNAIDsAS0NAED+ATONAEDxAT8KAIB9AUB5AEDAAVUNAEBdAVZzAECCAWt9AAAA","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FACE_1 = """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA"}],"format":"ROC_1_23","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FACE_2 = """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA"}],"format":"ROC_3","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_1 = """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"b26c91bc-b307-4131-80c3-55090ba5dbf2","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":99,"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA","finger":"LEFT_THUMB"},{"quality":88,"template":"Rk1SACAyMAAAAAEUAAABLAGQAMUAxQEAABBYKUBqACiHAIBKAD0RAECyAEjuAEAfAF6sAEAnAG4sAIB4AHT2AIByAIuiAEAGAIs1AEC9AJDUAEB3AJS9AICaAJXRAEDzAJ/bAECHAKi9AIBzALJDAEAXALfDAEB6ALszAIDGAL3JAED2AL/QAEBHAMrRAEENAMrWAECiAMy9AIAkANfNAEDwANnKAEB3AOr4AICjAPKhAEANAPXGAIEEAPjJAEBCAPzaAICNAQUDAEEZAQdpAEDOARsfAIDpAR0TAEDZASUUAEDnASeNAIDsAS0NAED+ATONAEDxAT8KAIB9AUB5AEDAAVUNAEBdAVZzAECCAWt9AAAA","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"},{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA"}],"format":"ROC_1_23","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""
        private const val SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_2 = """{"events":[{"id":"0dafcd03-96c4-4ca5-b802-292da6d4f799","payload":{"subjectId":"a961fcb4-8573-4270-a1b2-088e88275b00","projectId":"nXcj9neYhXP9rFp56uWk","moduleId":{"value":"AWuA3H0WGtHI2uod+ePZ3yiWTt9etQ=="},"attendantId":{"value":"AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzSr9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88="},"biometricReferences":[{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"quality":77,"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA","finger":"LEFT_THUMB"},{"quality":66,"template":"Rk1SACAyMAAAAAEUAAABLAGQAMUAxQEAABBYKUBqACiHAIBKAD0RAECyAEjuAEAfAF6sAEAnAG4sAIB4AHT2AIByAIuiAEAGAIs1AEC9AJDUAEB3AJS9AICaAJXRAEDzAJ/bAECHAKi9AIBzALJDAEAXALfDAEB6ALszAIDGAL3JAED2AL/QAEBHAMrRAEENAMrWAECiAMy9AIAkANfNAEDwANnKAEB3AOr4AICjAPKhAEANAPXGAIEEAPjJAEBCAPzaAICNAQUDAEEZAQdpAEDOARsfAIDpAR0TAEDZASUUAEDnASeNAIDsAS0NAED+ATONAEDxAT8KAIB9AUB5AEDAAVUNAEBdAVZzAECCAWt9AAAA","finger":"LEFT_INDEX_FINGER"}],"format":"ISO_19794_2","type":"FINGERPRINT_REFERENCE"},{"id":"2b9b4991-29d7-3eee-ac02-191afaa0c1a2","templates":[{"template":"Rk1SACAyMAAAAADkAAABLAGQAMUAxQEAABBjIYCzAAgQAEAhABoeAIB3ACQNAEAoACsYAEDvADYDAECrAFgDAEBQAHIaAEDUAH2AAEEEAIX4AEDhAJX4AEDpALZ7AIB1AL0RAEEJAMPuAECSAM0NAED2ANPxAEDmAOXsAEDdAPrnAEBhAQYaAIDcARDaAICwARn8AEDJASlnAECMATcVAEB7ATgbAECxAT2qAECnAT6hAEB3AT6eAECIAT6eAEEWAUDGAIDHAUizAED9AU2zAEDsAVU9AED1AWYzAIC2AWciAAAA"}],"format":"ROC_3","type":"FACE_REFERENCE"}]},"type":"EnrolmentRecordCreation"}]}"""

        private val expectedFingerprintIdentities = listOf(
            FingerprintIdentity(
                subjectId = "b26c91bc-b307-4131-80c3-55090ba5dbf2",
                fingerprints =
                listOf(
                    FingerprintSample(
                        fingerIdentifier = LEFT_THUMB,
                        templateQualityScore = 99,
                        template = byteArrayOf(),
                        format = "ISO_19794_2"
                    ),
                    FingerprintSample(
                        fingerIdentifier = LEFT_INDEX_FINGER,
                        templateQualityScore = 88,
                        template = byteArrayOf(),
                        format = "ISO_19794_2"
                    )
                )
            ),
            FingerprintIdentity(
                subjectId = "a961fcb4-8573-4270-a1b2-088e88275b00",
                fingerprints =
                listOf(
                    FingerprintSample(
                        fingerIdentifier = LEFT_THUMB,
                        templateQualityScore = 77,
                        template = byteArrayOf(),
                        format = "ISO_19794_2"
                    ),
                    FingerprintSample(
                        fingerIdentifier = LEFT_INDEX_FINGER,
                        templateQualityScore = 66,
                        template = byteArrayOf(),
                        format = "ISO_19794_2"
                    )
                )
            ),
        )
        val expectedFaceIdentities = listOf(
            FaceIdentity(
                subjectId = "b26c91bc-b307-4131-80c3-55090ba5dbf2",
                faces = listOf(FaceSample(template = byteArrayOf(), format = "ROC_1_23"))
            ),
            FaceIdentity(
                subjectId = "a961fcb4-8573-4270-a1b2-088e88275b00",
                faces = listOf(FaceSample(template = byteArrayOf(), format = "ROC_3"))
            ),
        )

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

    @MockK
    private lateinit var encoder: EncodingUtils

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var mockContentResolver: ContentResolver

    private lateinit var mockMetadataCursor: Cursor

    private lateinit var mockDataCursor: Cursor

    private lateinit var dataSource: CommCareIdentityDataSource

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

        every { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) } returns mockMetadataCursor
        every { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) } returns mockDataCursor

        every { encoder.base64ToBytes(any()) } returns byteArrayOf()

        dataSource = CommCareIdentityDataSource(encoder, JsonHelper, context)
    }

    @Test
    fun testLoadFingerprintIdentities() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2)

        val query = SubjectQuery()
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(expectedFingerprintIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                    expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                        expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                        expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                        expectedFingerprint.format == actualFingerprint.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadFingerprintIdentities for specific subject ID`() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2)

        val query = SubjectQuery(subjectId = "a961fcb4-8573-4270-a1b2-088e88275b00")
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(1, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities
            .filter { it.subjectId == "a961fcb4-8573-4270-a1b2-088e88275b00" }
            .zip(actualIdentities) { expected, actual ->
                expected.subjectId == actual.subjectId &&
                    expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                        expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                            expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                            expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                            expectedFingerprint.format == actualFingerprint.format
                    }.all { it }
            }.all { it }
        assertTrue(areContentsEqual)
    }

    @Test
    fun `test loadFingerprintIdentities for specific attendant ID`() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2)

        val query = SubjectQuery(attendantId = "r9MYXl5maWEpyKQT8AUdcPuVHUWpOkO88AdySMrjuy7uq0Dcxov3rUFIw66uXTFrKd0BnzS=")
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(1, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities
            .filter { it.subjectId == "a961fcb4-8573-4270-a1b2-088e88275b00" }
            .zip(actualIdentities) { expected, actual ->
                expected.subjectId == actual.subjectId &&
                    expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                        expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                            expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                            expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                            expectedFingerprint.format == actualFingerprint.format
                    }.all { it }
            }.all { it }
        assertTrue(areContentsEqual)
    }

    @Test
    fun `test loadFingerprintIdentities for specific module ID`() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2)

        val query = SubjectQuery(moduleId = "ePZ3yiWTt9etQAWu+A3H0WGtHI2uod==")
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(1, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities
            .filter { it.subjectId == "a961fcb4-8573-4270-a1b2-088e88275b00" }
            .zip(actualIdentities) { expected, actual ->
                expected.subjectId == actual.subjectId &&
                    expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                        expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                            expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                            expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                            expectedFingerprint.format == actualFingerprint.format
                    }.all { it }
            }.all { it }
        assertTrue(areContentsEqual)
    }

    @Test
    fun `test load fingerprint fetches case by providedID`() = runTest {
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2)

        val query = SubjectQuery(metadata = """{"caseId":"CASE"}""")
        val range = 0..1
        dataSource.loadFingerprintIdentities(query, range)

        coVerify(exactly = 0) { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun testLoadFaceIdentities() = runTest {
        every { mockMetadataCursor.count } returns expectedFaceIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FACE_1, SUBJECT_ACTIONS_FACE_2)

        val query = SubjectQuery()
        val range = 0..expectedFaceIdentities.size
        val actualIdentities = dataSource.loadFaceIdentities(query, range)

        assertEquals(expectedFaceIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFaceIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.faces.zip(actual.faces) { expectedFace, actualFace ->
                    expectedFace.template.contentEquals(actualFace.template) &&
                        expectedFace.format == actualFace.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadFingerprintIdentities returns only identities with fingerprint references`() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size + 1
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2, SUBJECT_ACTIONS_FACE_1)

        val query = SubjectQuery()
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(expectedFingerprintIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                    expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                        expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                        expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                        expectedFingerprint.format == actualFingerprint.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadFaceIdentities returns only identities with face references`() = runTest {
        every { mockMetadataCursor.count } returns expectedFaceIdentities.size + 1
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FACE_1, SUBJECT_ACTIONS_FACE_2, SUBJECT_ACTIONS_FINGERPRINT_1)

        val query = SubjectQuery()
        val range = 0..expectedFaceIdentities.size
        val actualIdentities = dataSource.loadFaceIdentities(query, range)

        assertEquals(expectedFaceIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFaceIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.faces.zip(actual.faces) { expectedFace, actualFace ->
                    expectedFace.template.contentEquals(actualFace.template) &&
                        expectedFace.format == actualFace.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadFingerprintIdentities returns only fingerprint references for dual modality identities`() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_1, SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_2)

        val query = SubjectQuery()
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(expectedFingerprintIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                    expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                        expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                        expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                        expectedFingerprint.format == actualFingerprint.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test loadFaceIdentities returns only face references for dual modality identities`() = runTest {
        every { mockMetadataCursor.count } returns expectedFaceIdentities.size
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_1, SUBJECT_ACTIONS_FINGERPRINT_AND_FACE_2)

        val query = SubjectQuery()
        val range = 0..expectedFaceIdentities.size
        val actualIdentities = dataSource.loadFaceIdentities(query, range)

        assertEquals(expectedFaceIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFaceIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.faces.zip(actual.faces) { expectedFace, actualFace ->
                    expectedFace.template.contentEquals(actualFace.template) &&
                        expectedFace.format == actualFace.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun testCount() = runTest {
        val expectedCount = 5
        every { mockMetadataCursor.count } returns expectedCount

        val query = SubjectQuery()
        val actualCount = dataSource.count(query)

        assertEquals(expectedCount, actualCount)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockMetadataCursor.count }
    }

    @Test
    fun `test handling of null metadata cursor`() = runTest {
        every { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) } returns null

        val query = SubjectQuery()
        val range = 0..0
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertTrue(actualIdentities.isEmpty())
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test metadata cursor size below range's first`() = runTest {
        every { mockMetadataCursor.count } returns 1

        val query = SubjectQuery()
        val range = 2..3
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertTrue(actualIdentities.isEmpty())
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `test metadata cursor size bigger than range`() = runTest {
        every { mockMetadataCursor.count } returns expectedFingerprintIdentities.size + 1
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, true)
        every { mockMetadataCursor.position } returnsMany listOf(1, 2)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf(SUBJECT_ACTIONS_FINGERPRINT_1, SUBJECT_ACTIONS_FINGERPRINT_2)

        val query = SubjectQuery()
        val range = 0..expectedFingerprintIdentities.size
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(expectedFingerprintIdentities.size, actualIdentities.size)
        val areContentsEqual = expectedFingerprintIdentities.zip(actualIdentities) { expected, actual ->
            expected.subjectId == actual.subjectId &&
                expected.fingerprints.zip(actual.fingerprints) { expectedFingerprint, actualFingerprint ->
                    expectedFingerprint.fingerIdentifier == actualFingerprint.fingerIdentifier &&
                        expectedFingerprint.templateQualityScore == actualFingerprint.templateQualityScore &&
                        expectedFingerprint.template.contentEquals(actualFingerprint.template) &&
                        expectedFingerprint.format == actualFingerprint.format
                }.all { it }
        }.all { it }
        assertTrue(areContentsEqual)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `empty caseId results in empty result`() = runTest {
        every { mockMetadataCursor.count } returns 2
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockMetadataCursor.getString(any()) } returns null

        val query = SubjectQuery()
        val range = 0..2
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `exception during metadata cursor access is reported`() = runTest {
        every { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) } throws
            RuntimeException("Some exception")

        val query = SubjectQuery()
        val range = 0..2
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify(exactly = 0) { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
        coVerify { Simber.e(any(), ofType<RuntimeException>()) }
    }

    @Test
    fun `data cursor is null`() = runTest {
        every { mockMetadataCursor.count } returns 2
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) } returns null

        val query = SubjectQuery()
        val range = 0..2
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `subjectActions not found in cursor data`() = runTest {
        every { mockMetadataCursor.count } returns 2
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returnsMany listOf(true, true, true, false)
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returns "someKey"
        every { mockDataCursor.getString(1) } returns "someValue"

        val query = SubjectQuery()
        val range = 0..2
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
    }

    @Test
    fun `subjectActions contains invalid JSON`() = runTest {
        every { mockMetadataCursor.count } returns 2
        every { mockMetadataCursor.moveToPosition(0) } returns true
        every { mockMetadataCursor.moveToNext() } returnsMany listOf(true, false)
        every { mockDataCursor.moveToNext() } returns true
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_DATUM_ID) } returns 0
        every { mockDataCursor.getColumnIndexOrThrow(COLUMN_VALUE) } returns 1
        every { mockDataCursor.getString(0) } returnsMany
            listOf("someOtherDatumId", "subjectActions", "someOtherDatumId", "subjectActions")
        every { mockDataCursor.getString(1) } returnsMany
            listOf("invalid JSON 1", "invalid JSON 2")

        val query = SubjectQuery()
        val range = 0..2
        val actualIdentities = dataSource.loadFingerprintIdentities(query, range)

        assertEquals(0, actualIdentities.size)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
        coVerify { mockContentResolver.query(mockDataCaseIdUri, any(), any(), any(), any()) }
        coVerify { Simber.e(any(), ofType<Exception>()) }
    }

    @Test
    fun `null metadata cursor during count`() = runTest {
        every { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) } returns null

        val query = SubjectQuery()
        val actualCount = dataSource.count(query)

        assertEquals(0, actualCount)
        coVerify { mockContentResolver.query(mockMetadataUri, any(), any(), any(), any()) }
    }
}
