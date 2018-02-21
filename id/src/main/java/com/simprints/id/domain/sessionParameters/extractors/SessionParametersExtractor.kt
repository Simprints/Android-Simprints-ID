package com.simprints.id.domain.sessionParameters.extractors

import com.simprints.id.domain.callout.Callout
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.sessionParameters.SessionParameters


class SessionParametersExtractor(private val actionExtractor: Extractor<CalloutAction>,
                                 private val apiKeyExtractor: Extractor<String>,
                                 private val projectIdExtractor: Extractor<String>,
                                 private val moduleIdExtractor: Extractor<String>,
                                 private val userIdExtractor: Extractor<String>,
                                 private val patientIdExtractor: Extractor<String>,
                                 private val callingPackageExtractor: Extractor<String>,
                                 private val metadataExtractor: Extractor<String>,
                                 private val resultFormatExtractor: Extractor<String>,
                                 private val unexpectedParametersExtractor: Extractor<Set<CalloutParameter>>)
    : Extractor<SessionParameters> {

    override fun extractFrom(callout: Callout): SessionParameters {
        val action = actionExtractor.extractFrom(callout)
        val projectId = projectIdExtractor.extractFrom(callout)
        var apiKey = ""
        if(projectId.isEmpty()) {
            apiKey = apiKeyExtractor.extractFrom(callout)
        }
        val moduleId = moduleIdExtractor.extractFrom(callout)
        val userId = userIdExtractor.extractFrom(callout)
        val patientId = patientIdExtractor.extractFrom(callout)
        val callingPackage = callingPackageExtractor.extractFrom(callout)
        val metadata = metadataExtractor.extractFrom(callout)
        val resultFormat = resultFormatExtractor.extractFrom(callout)
        unexpectedParametersExtractor.extractFrom(callout)

        return SessionParameters(action, apiKey, projectId, moduleId, userId, patientId, callingPackage,
            metadata, resultFormat)
    }

}
