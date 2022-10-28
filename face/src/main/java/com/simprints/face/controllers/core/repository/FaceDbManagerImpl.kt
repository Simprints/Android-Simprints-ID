package com.simprints.face.controllers.core.repository

import com.simprints.face.data.db.person.FaceIdentity
import com.simprints.face.data.db.person.FaceSample
import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import javax.inject.Inject


class FaceDbManagerImpl @Inject constructor(private val enrolmentRecordManager: EnrolmentRecordManager) :
    FaceDbManager {

    override suspend fun loadPeople(query: Serializable): Flow<FaceIdentity> =
        enrolmentRecordManager
            .loadFaceIdentities(query)
            .map {
                FaceIdentity(
                    it.personId,
                    it.faces.map { face -> FaceSample(face.id, face.template) })
            }
}

