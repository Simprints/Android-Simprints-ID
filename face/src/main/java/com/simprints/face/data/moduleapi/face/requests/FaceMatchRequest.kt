package com.simprints.face.data.moduleapi.face.requests

import com.simprints.face.data.db.person.FaceSample
import com.simprints.moduleapi.face.responses.entities.IFaceSample
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class FaceMatchRequest(val probeFaceSamples: List<FaceSample>,
                            val queryForCandidates: Serializable) : FaceRequest

fun IFaceSample.fromModuleApiToDomainFaceSample() = FaceSample(faceId, template)
