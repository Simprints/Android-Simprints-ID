package com.simprints.id.orchestrator

import com.simprints.id.data.db.subject.domain.Subject
import com.simprints.id.domain.moduleapi.face.responses.FaceCaptureResponse
import com.simprints.id.domain.moduleapi.fingerprint.responses.FingerprintCaptureResponse
import com.simprints.id.tools.TimeHelper

interface EnrolmentHelper {

    fun buildSubject(projectId: String,
                     userId: String,
                     moduleId: String,
                     fingerprintResponse: FingerprintCaptureResponse?,
                     faceResponse: FaceCaptureResponse?,
                     timeHelper: TimeHelper): Subject

    suspend fun enrol(subject: Subject)
}
