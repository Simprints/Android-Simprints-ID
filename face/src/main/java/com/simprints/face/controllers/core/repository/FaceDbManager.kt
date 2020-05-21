package com.simprints.face.controllers.core.repository

import com.simprints.face.data.db.person.FaceRecord
import java.io.Serializable

interface FaceDbManager {
    suspend fun loadPeople(query: Serializable): List<FaceRecord>
}
