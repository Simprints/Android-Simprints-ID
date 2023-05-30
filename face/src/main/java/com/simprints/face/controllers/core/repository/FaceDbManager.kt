package com.simprints.face.controllers.core.repository

import com.simprints.infra.facebiosdk.matching.FaceIdentity
import kotlinx.coroutines.flow.Flow
import java.io.Serializable

fun interface FaceDbManager {
    suspend fun loadPeople(query: Serializable): Flow<FaceIdentity>
}
