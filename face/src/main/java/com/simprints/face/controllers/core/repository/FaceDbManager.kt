package com.simprints.face.controllers.core.repository

import com.simprints.infra.facebiosdk.matching.FaceIdentity
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

interface FaceDbManager {
    suspend fun loadPeople(query: Serializable): Flow<FaceIdentity>
}
