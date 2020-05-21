package com.simprints.face.controllers.core.repository

import com.simprints.face.data.db.person.FaceIdentity
import java.io.Serializable

interface FaceDbManager {
    suspend fun loadPeople(query: Serializable): List<FaceIdentity>
}
