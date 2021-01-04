package com.simprints.fingerprint.controllers.core.repository

import com.simprints.fingerprint.data.domain.fingerprint.FingerprintIdentity
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FingerprintDbManager {

    suspend fun loadPeople(query: Serializable): Flow<FingerprintIdentity>
}
