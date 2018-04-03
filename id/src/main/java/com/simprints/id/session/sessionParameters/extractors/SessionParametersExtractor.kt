package com.simprints.id.session.sessionParameters.extractors

import com.simprints.id.session.callout.Callout
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameter
import com.simprints.id.session.sessionParameters.SessionParameters
import com.simprints.id.session.sessionParameters.validators.Validator

class SessionParametersExtractor(private val actionExtractor: Extractor<CalloutAction>,
                                 private val apiKeyExtractor: Extractor<String>,
                                 private val projectIdExtractor: Extractor<String>,
                                 private val moduleIdExtractor: Extractor<String>,
                                 private val userIdExtractor: Extractor<String>,
                                 private val patientIdExtractor: Extractor<String>,
                                 private val callingPackageExtractor: Extractor<String>,
                                 private val metadataExtractor: Extractor<String>,
                                 private val resultFormatExtractor: Extractor<String>,
                                 private val unexpectedParametersExtractor: Extractor<Set<CalloutParameter>>,
                                 private val sessionParametersValidators: Set<Validator<SessionParameters>>)
    : Extractor<SessionParameters> {

    override fun extractFrom(callout: Callout): SessionParameters {
        val action = actionExtractor.extractFrom(callout)
        val projectId = projectIdExtractor.extractFrom(callout)
        val apiKey = apiKeyExtractor.extractFrom(callout)
        val moduleId = moduleIdExtractor.extractFrom(callout)
        val userId = userIdExtractor.extractFrom(callout)
        val patientId = patientIdExtractor.extractFrom(callout)
        val callingPackage = callingPackageExtractor.extractFrom(callout)
        val metadata = metadataExtractor.extractFrom(callout)
        val resultFormat = resultFormatExtractor.extractFrom(callout)
        unexpectedParametersExtractor.extractFrom(callout)

        val sessionParameters = SessionParameters(action, apiKey, projectId, moduleId, userId, patientId, callingPackage, metadata, resultFormat)
        sessionParametersValidators.forEach { it.validate(sessionParameters) }
        return sessionParameters
    }
}
