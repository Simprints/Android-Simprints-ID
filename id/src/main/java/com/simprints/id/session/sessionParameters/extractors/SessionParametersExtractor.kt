package com.simprints.id.session.sessionParameters.extractors

import com.simprints.id.domain.requests.IdRequest
import com.simprints.id.session.callout.Callout
import com.simprints.id.session.sessionParameters.SessionParameters

class SessionParametersExtractorImpl: SessionParametersExtractor {

    override fun extractFrom(idRequest: IdRequest, callingPackage: String, resultFormat: String): SessionParameters {
        val action = Callout.getCalloutAction(idRequest)
        val projectId = idRequest.projectId
        val moduleId = idRequest.moduleId
        val userId = idRequest.userId
        val metadata = idRequest.metadata

        //STOPSHIP: Remove me
        return SessionParameters(action, "", projectId, moduleId, userId, "", callingPackage, metadata, "")
    }
}

interface SessionParametersExtractor {
    fun extractFrom(idRequest: IdRequest, callingPackage: String, resultFormat: String): SessionParameters
}
