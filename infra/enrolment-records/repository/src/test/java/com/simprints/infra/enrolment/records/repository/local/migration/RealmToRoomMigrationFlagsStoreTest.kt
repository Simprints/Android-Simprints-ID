package com.simprints.infra.enrolment.records.repository.local.migration

import android.content.SharedPreferences
import com.google.common.truth.Truth.*
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.ExperimentalProjectConfiguration.Companion.RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES
import com.simprints.infra.config.store.models.ProjectConfiguration
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RealmToRoomMigrationFlagsStoreTest {
    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var configRepo: ConfigRepository

    @MockK(relaxUnitFun = true)
    private lateinit var sharedPreferences: SharedPreferences

    @MockK(relaxUnitFun = true)
    private lateinit var editor: SharedPreferences.Editor

    @MockK
    private lateinit var mockProjectConfig: ProjectConfiguration

    private lateinit var store: RealmToRoomMigrationFlagsStore

    private val experimentalFeaturesMap = mutableMapOf<String, Any>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { securityManager.buildEncryptedSharedPreferences(any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.putInt(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.putBoolean(any(), any()) } returns editor

        coEvery { mockProjectConfig.custom } returns experimentalFeaturesMap
        coEvery { configRepo.getProjectConfiguration() } returns mockProjectConfig

        store = RealmToRoomMigrationFlagsStore(securityManager, configRepo)
    }

    @Test
    fun `getCurrentStatus should return NOT_STARTED when no status is saved`() = runTest {
        // Given
        every {
            sharedPreferences.getString(
                RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS,
                MigrationStatus.NOT_STARTED.name,
            )
        } returns MigrationStatus.NOT_STARTED.name

        // When
        val status = store.getCurrentStatus()

        // Then
        assertThat(status).isEqualTo(MigrationStatus.NOT_STARTED)
        verify { sharedPreferences.getString(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS, MigrationStatus.NOT_STARTED.name) }
    }

    @Test
    fun `getCurrentStatus should return saved status`() = runTest {
        // Given
        val expectedStatus = MigrationStatus.IN_PROGRESS
        every {
            sharedPreferences.getString(
                RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS,
                MigrationStatus.NOT_STARTED.name,
            )
        } returns expectedStatus.name

        // When
        val status = store.getCurrentStatus()

        // Then
        assertThat(status).isEqualTo(expectedStatus)
        verify { sharedPreferences.getString(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS, MigrationStatus.NOT_STARTED.name) }
    }

    @Test
    fun `isMigrationInProgress should return true when status is IN_PROGRESS`() = runTest {
        // Given
        every {
            sharedPreferences.getString(
                RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS,
                MigrationStatus.NOT_STARTED.name,
            )
        } returns MigrationStatus.IN_PROGRESS.name

        // When
        val isInProgress = store.isMigrationInProgress()

        // Then
        assertThat(isInProgress).isTrue()
    }

    @Test
    fun `isMigrationInProgress should return false when status is not IN_PROGRESS`() = runTest {
        // Given
        every {
            sharedPreferences.getString(
                RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS,
                MigrationStatus.NOT_STARTED.name,
            )
        } returns MigrationStatus.COMPLETED.name

        // When
        val isInProgress = store.isMigrationInProgress()

        // Then
        assertThat(isInProgress).isFalse()
    }

    @Test
    fun `isMigrationCompleted should return true when status is COMPLETED`() = runTest {
        // Given
        every {
            sharedPreferences.getString(
                RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS,
                MigrationStatus.NOT_STARTED.name,
            )
        } returns MigrationStatus.COMPLETED.name

        // When
        val isCompleted = store.isMigrationCompleted()

        // Then
        assertThat(isCompleted).isTrue()
    }

    @Test
    fun `isMigrationCompleted should return false when status is not COMPLETED`() = runTest {
        // Given
        every {
            sharedPreferences.getString(
                RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS,
                MigrationStatus.NOT_STARTED.name,
            )
        } returns MigrationStatus.IN_PROGRESS.name

        // When
        val isCompleted = store.isMigrationCompleted()

        // Then
        assertThat(isCompleted).isFalse()
    }

    @Test
    fun `updateStatus should save the correct status string`() = runTest {
        // Given
        val newStatus = MigrationStatus.FAILED

        // When
        store.updateStatus(newStatus)

        // Then
        verify { editor.putString(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_STATUS, newStatus.name) }
    }

    @Test
    fun `incrementRetryCount should increase retry count from 0`() = runTest {
        // Given
        every { sharedPreferences.getInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, 0) } returns 0

        // When
        store.incrementRetryCount()

        // Then
        verify { editor.putInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, 1) }
        verify { editor.apply() }
    }

    @Test
    fun `incrementRetryCount should increase retry count from existing value`() = runTest {
        // Given
        val currentRetries = 2
        every { sharedPreferences.getInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, 0) } returns currentRetries

        // When
        store.incrementRetryCount()

        // Then
        verify { editor.putInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, currentRetries + 1) }
        verify { editor.apply() }
    }

    @Test
    fun `resetRetryCount should remove the retry count key`() = runTest {
        // When
        store.resetRetryCount()

        // Then
        verify { editor.remove(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES) }
    }

    @Test
    fun `canRetry should return true when retries are less than max`() = runTest {
        // Given
        val maxRetries = 5
        experimentalFeaturesMap[RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES] = maxRetries
        val currentRetries = maxRetries - 1
        every { sharedPreferences.getInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, 0) } returns currentRetries

        // When
        val result = store.canRetry()

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun `canRetry should return false when retries reach max`() = runTest {
        // Given
        val maxRetries = 5
        experimentalFeaturesMap[RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES] = maxRetries

        val currentRetries = maxRetries
        every { sharedPreferences.getInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, 0) } returns currentRetries

        // When
        val result = store.canRetry()

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `canRetry should return false when retries exceed max`() = runTest {
        // Given
        val maxRetries = 5
        experimentalFeaturesMap[RECORDS_DB_MIGRATION_FROM_REALM_TO_ROOM_MAX_RETRIES] = maxRetries

        val currentRetries = maxRetries + 1
        every { sharedPreferences.getInt(RealmToRoomMigrationFlagsStore.KEY_MIGRATION_RETRIES, 0) } returns currentRetries

        // When
        val result = store.canRetry()

        // Then
        assertThat(result).isFalse()
    }

    @Test
    fun `isDownSyncInProgress should return false when no status is saved`() = runTest {
        // Given: No down sync status is stored (default behavior)
        every { sharedPreferences.getBoolean(RealmToRoomMigrationFlagsStore.KEY_DOWN_SYNC_STATUS, false) } returns false

        // When: isDownSyncInProgress is called
        val result = store.isDownSyncInProgress()

        // Then: It should return false
        assertThat(result).isFalse()
        verify { sharedPreferences.getBoolean(RealmToRoomMigrationFlagsStore.KEY_DOWN_SYNC_STATUS, false) }
    }

    @Test
    fun `isDownSyncInProgress should return true when status is saved as true`() = runTest {
        // Given: Down sync status is stored as true
        every { sharedPreferences.getBoolean(RealmToRoomMigrationFlagsStore.KEY_DOWN_SYNC_STATUS, false) } returns true

        // When: isDownSyncInProgress is called
        val result = store.isDownSyncInProgress()

        // Then: It should return true
        assertThat(result).isTrue()
        verify { sharedPreferences.getBoolean(RealmToRoomMigrationFlagsStore.KEY_DOWN_SYNC_STATUS, false) }
    }

    @Test
    fun `setDownSyncInProgress should save true`() = runTest {
        // Given: The status to save is true
        val isInProgress = true

        // When: setDownSyncInProgress is called with true
        store.setDownSyncInProgress(isInProgress)

        // Then: The editor should putBoolean with true
        verify { editor.putBoolean(RealmToRoomMigrationFlagsStore.KEY_DOWN_SYNC_STATUS, true) }
        verify { editor.apply() }
    }

    @Test
    fun `setDownSyncInProgress should save false`() = runTest {
        // Given: The status to save is false
        val isInProgress = false

        // When: setDownSyncInProgress is called with false
        store.setDownSyncInProgress(isInProgress)

        // Then: The editor should putBoolean with false
        verify { editor.putBoolean(RealmToRoomMigrationFlagsStore.KEY_DOWN_SYNC_STATUS, false) }
        verify { editor.apply() }
    }

    @Test
    fun `clearMigrationFlags should remove all migration-related keys`() {
        // Given
        every { editor.clear() } returns editor
        // When
        store.clearMigrationFlags()
        // Then
        verify { editor.clear() }
    }
}
