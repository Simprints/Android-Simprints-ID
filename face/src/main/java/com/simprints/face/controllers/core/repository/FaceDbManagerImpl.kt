package com.simprints.face.controllers.core.repository

import com.simprints.face.data.db.person.FaceRecord
import com.simprints.face.data.moduleapi.face.responses.entities.FaceSample
import com.simprints.id.data.db.person.local.FaceRecordLocalDataSource
import kotlinx.coroutines.flow.toList
import java.io.Serializable
import com.simprints.id.data.db.person.domain.FaceSample as CoreFaceSample

class FaceDbManagerImpl(private val coreFingerprintIdentityLocalDataSource: FaceRecordLocalDataSource) : FaceDbManager {

    override suspend fun loadPeople(query: Serializable): List<FaceRecord> =
        coreFingerprintIdentityLocalDataSource
            .loadFaceRecords(query)
            .toList()
            .map {
                FaceRecord(it.personId, it.faces.map { face -> face.fromCoreToDomain() })
            }
}

fun CoreFaceSample.fromCoreToDomain() = FaceSample(id, template, null)
