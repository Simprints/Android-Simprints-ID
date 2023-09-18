package com.simprints.face.matcher.usecases

import com.simprints.infra.enrolment.records.EnrolmentRecordManager
import com.simprints.infra.facebiosdk.matching.FaceIdentity
import com.simprints.infra.facebiosdk.matching.FaceSample
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.Serializable
import javax.inject.Inject

internal class LoadPeopleFaceIdentityUseCase @Inject constructor(
    private val enrolmentRecordManager: EnrolmentRecordManager
) {

    suspend operator fun invoke(query: Serializable): Flow<FaceIdentity> = enrolmentRecordManager
        .loadFaceIdentities(query)
        .map {
            FaceIdentity(
                it.personId,
                it.faces.map { face -> FaceSample(face.id, face.template) }
            )
        }
}
