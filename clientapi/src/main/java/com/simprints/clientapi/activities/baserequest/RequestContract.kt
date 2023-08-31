package com.simprints.clientapi.activities.baserequest

import com.simprints.clientapi.errors.ClientApiAlert
import com.simprints.clientapi.clientrequests.builders.ClientRequestBuilder
import com.simprints.clientapi.clientrequests.extractors.*
import com.simprints.clientapi.domain.requests.BaseRequest
import com.simprints.clientapi.domain.responses.*
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.Tokenization
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.enrolment.records.EnrolmentRecordManager

interface RequestContract {

    interface RequestView {
        val intentAction: String?
        val extras: Map<String, Any?>?
        val presenter: Presenter
        val enrolExtractor: EnrolExtractor
        val verifyExtractor: VerifyExtractor
        val identifyExtractor: IdentifyExtractor
        val confirmIdentityExtractor: ConfirmIdentityExtractor
        val enrolLastBiometricsExtractor: EnrolLastBiometricsExtractor
        val tokenization: Tokenization
        suspend fun getProject(): Project
        fun sendSimprintsRequest(request: BaseRequest)
        fun handleClientRequestError(clientApiAlert: ClientApiAlert)

        /**
         * This is not being used for Libsimprints and CommCare because of CoSync.
         * The methods [com.simprints.clientapi.activities.commcare.CommCareContract.View.returnErrorToClient]
         * and [com.simprints.clientapi.activities.libsimprints.LibSimprintsContract.View.returnErrorToClient]
         * are functionally the same as this, but you can pass a nullable eventJson that won't be included in the response
         * if it's null.
         */
        fun returnErrorToClient(
            errorResponse: ErrorResponse, flowCompletedCheck: Boolean, sessionId: String
        )
    }

    interface Presenter {
        suspend fun start()
        suspend fun processEnrolRequest()
        suspend fun processIdentifyRequest()
        suspend fun processVerifyRequest()
        suspend fun processConfirmIdentityRequest()
        suspend fun processEnrolLastBiometrics()
        fun handleEnrolResponse(enrol: EnrolResponse)
        fun handleIdentifyResponse(identify: IdentifyResponse)
        fun handleVerifyResponse(verify: VerifyResponse)
        fun handleRefusalResponse(refusalForm: RefusalFormResponse)
        fun handleResponseError(errorResponse: ErrorResponse)
        suspend fun validateAndSendRequest(builder: ClientRequestBuilder)
        fun handleConfirmationResponse(response: ConfirmationResponse)
        suspend fun getEventsJsonForSession(sessionId: String, jsonHelper: JsonHelper): String?

        fun getProjectIdFromRequest(): String
        suspend fun getEnrolmentCreationEventForSubject(
            subjectId: String,
            enrolmentRecordManager: EnrolmentRecordManager,
            timeHelper: ClientApiTimeHelper,
            jsonHelper: JsonHelper
        ): String?

        suspend fun deleteSessionEventsIfNeeded(sessionId: String)
    }
}
