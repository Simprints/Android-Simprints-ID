package com.simprints.infra.sync.devicestate

import kotlinx.coroutines.flow.Flow

/**
 * Single entry point for "how much data is on this device".
 *
 * For "what is the sync machinery currently doing" use [com.simprints.infra.sync.SyncOrchestrator.observeSyncState] instead -
 * this tracker deliberately knows nothing about WorkManager.
 */
interface DeviceStateDataTracker {
    /**
     * A freshly evaluated snapshot - never a replayed cache.
     *
     * Never throws: a source that fails is logged and its fields are left `null`.
     * More expensive than [observeDeviceDataState] because it walks the sample directory.
     */
    suspend fun getCurrentDeviceDataState(): DeviceDataState

    /** Shared, conflated stream for UI. */
    fun observeDeviceDataState(): Flow<DeviceDataState>

    /**
     * Number of enrolment records stored on the device, across every project.
     *
     * Deliberately unscoped, unlike [DeviceDataState.recordCount]: this feeds the session scope
     * payload, which must keep counting records left behind by an earlier project.
     * Propagates failures rather than degrading, matching the direct repository call it replaces.
     */
    suspend fun getRecordCount(): Int

    /**
     * Whether any events are still stored on the device.
     */
    suspend fun hasPendingEvents(): Boolean
}
