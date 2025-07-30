package com.simprints.infra.enrolment.records.repository.commcare

import android.content.SharedPreferences
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.enrolment.records.repository.commcare.models.CommCareCaseSyncInfo
import com.simprints.infra.security.SecurityManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CommCareCaseSyncCacheTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var jsonHelper: JsonHelper

    @MockK
    private lateinit var timeHelper: TimeHelper

    @MockK
    private lateinit var sharedPrefs: SharedPreferences

    @MockK
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var cache: CommCareCaseSyncCache

    private val testTimestamp = Timestamp(1234567890L)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        every { securityManager.buildEncryptedSharedPreferences(any()) } returns sharedPrefs
        every { sharedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.commit() } returns true
        every { timeHelper.now() } returns testTimestamp

        cache = CommCareCaseSyncCache(
            securityManager = securityManager,
            jsonHelper = jsonHelper,
            timeHelper = timeHelper,
            dispatcher = testCoroutineRule.testDispatcher
        )
    }

    @Test
    fun `saveCaseSyncInfo should save new case info`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val expectedInfo = CommCareCaseSyncInfo(caseId, subjectId, testTimestamp.ms)
        val expectedMap = mapOf(caseId to expectedInfo)
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns null
        every { jsonHelper.toJson(expectedMap) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject456\",\"lastModified\":1234567890}}"

        // When
        cache.saveCaseSyncInfo(caseId, subjectId)

        // Then
        verify { 
            sharedPrefs.edit()
            editor.putString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, any())
            editor.commit()
        }
        verify { jsonHelper.toJson(expectedMap) }
    }

    @Test
    fun `saveCaseSyncInfo should update existing case info`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val newSubjectId = "subject789"
        val existingInfo = CommCareCaseSyncInfo(caseId, subjectId, 999L)
        val existingMap = mapOf(caseId to existingInfo)
        val updatedInfo = CommCareCaseSyncInfo(caseId, newSubjectId, testTimestamp.ms)
        val expectedMap = mapOf(caseId to updatedInfo)
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject456\",\"lastModified\":999}}"
        every { jsonHelper.fromJson<Map<String, CommCareCaseSyncInfo>>(any(), any()) } returns existingMap
        every { jsonHelper.toJson(expectedMap) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject789\",\"lastModified\":1234567890}}"

        // When
        cache.saveCaseSyncInfo(caseId, newSubjectId)

        // Then
        verify { 
            sharedPrefs.edit()
            editor.putString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, any())
            editor.commit()
        }
        verify { jsonHelper.toJson(expectedMap) }
    }

    @Test
    fun `updateCaseLastModified should update existing case timestamp`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val existingInfo = CommCareCaseSyncInfo(caseId, subjectId, 999L)
        val existingMap = mapOf(caseId to existingInfo)
        val updatedInfo = CommCareCaseSyncInfo(caseId, subjectId, testTimestamp.ms)
        val expectedMap = mapOf(caseId to updatedInfo)
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject456\",\"lastModified\":999}}"
        every { jsonHelper.fromJson<Map<String, CommCareCaseSyncInfo>>(any(), any()) } returns existingMap
        every { jsonHelper.toJson(expectedMap) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject456\",\"lastModified\":1234567890}}"

        // When
        cache.updateCaseLastModified(caseId)

        // Then
        verify { 
            sharedPrefs.edit()
            editor.putString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, any())
            editor.commit()
        }
        verify { jsonHelper.toJson(expectedMap) }
    }

    @Test
    fun `deleteCaseSyncInfo should remove case from cache`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val existingInfo = CommCareCaseSyncInfo(caseId, subjectId, 999L)
        val existingMap = mapOf(caseId to existingInfo)
        val expectedMap = emptyMap<String, CommCareCaseSyncInfo>()
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject456\",\"lastModified\":999}}"
        every { jsonHelper.fromJson<Map<String, CommCareCaseSyncInfo>>(any(), any()) } returns existingMap
        every { jsonHelper.toJson(expectedMap) } returns "{}"

        // When
        cache.deleteCaseSyncInfo(caseId)

        // Then
        verify { 
            sharedPrefs.edit()
            editor.putString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, any())
            editor.commit()
        }
        verify { jsonHelper.toJson(expectedMap) }
    }

    @Test
    fun `getCaseSyncInfo should return case info if exists`() = runTest {
        // Given
        val caseId = "case123"
        val subjectId = "subject456"
        val expectedInfo = CommCareCaseSyncInfo(caseId, subjectId, 999L)
        val existingMap = mapOf(caseId to expectedInfo)
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns "{\"case123\":{\"caseId\":\"case123\",\"subjectId\":\"subject456\",\"lastModified\":999}}"
        every { jsonHelper.fromJson<Map<String, CommCareCaseSyncInfo>>(any(), any()) } returns existingMap

        // When
        val result = cache.getCaseSyncInfo(caseId)

        // Then
        assertEquals(expectedInfo, result)
    }

    @Test
    fun `getCaseSyncInfo should return null if case does not exist`() = runTest {
        // Given
        val caseId = "nonexistent"
        val existingMap = emptyMap<String, CommCareCaseSyncInfo>()
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns null
        every { jsonHelper.fromJson<Map<String, CommCareCaseSyncInfo>>(any(), any()) } returns existingMap

        // When
        val result = cache.getCaseSyncInfo(caseId)

        // Then
        assertNull(result)
    }

    @Test
    fun `getAllCaseSyncInfo should return all cases`() = runTest {
        // Given
        val expectedMap = mapOf(
            "case1" to CommCareCaseSyncInfo("case1", "subject1", 111L),
            "case2" to CommCareCaseSyncInfo("case2", "subject2", 222L)
        )
        
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns "{\"case1\":{},\"case2\":{}}"
        every { jsonHelper.fromJson<Map<String, CommCareCaseSyncInfo>>(any(), any()) } returns expectedMap

        // When
        val result = cache.getAllCaseSyncInfo()

        // Then
        assertEquals(expectedMap, result)
    }

    @Test
    fun `getAllCaseSyncInfo should return empty map when no data stored`() = runTest {
        // Given
        every { sharedPrefs.getString(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY, null) } returns null

        // When
        val result = cache.getAllCaseSyncInfo()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `clearAllCaseSyncInfo should remove all data`() = runTest {
        // When
        cache.clearAllCaseSyncInfo()

        // Then
        verify { 
            sharedPrefs.edit()
            editor.remove(CommCareCaseSyncCache.CASE_SYNC_INFO_KEY)
            editor.commit()
        }
    }
}