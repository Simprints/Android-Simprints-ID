package com.simprints.id.orchestrator

import com.simprints.id.data.db.person.domain.Person
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.tools.TimeHelper

interface EnrolmentHelper {

    fun buildPerson(projectId: String,
                    userId: String,
                    moduleId: String,
                    fingerprintResponse: FingerprintCaptureResponse?,
                    faceResponse: FaceCaptureResponse?,
                    timeHelper: TimeHelper): Person

    suspend fun enrol(person: Person)
}
