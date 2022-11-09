package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

/**
 * This interface represents a manager for stored subject fingerprint ids
 */
interface FingerprintDbManager {

    suspend fun loadSubjects(query: Serializable): Flow<FingerprintIdentity>
}
