package com.simprints.id.session.callout

import android.content.Intent
import com.simprints.id.domain.requests.*
import com.simprints.id.session.callout.CalloutAction.Companion.calloutAction
import com.simprints.id.session.callout.CalloutParameters.Companion.calloutParameters

@Deprecated("Replaced by domain.request classes")
class Callout(val action: CalloutAction, val parameters: CalloutParameters) {

    constructor(request: IdRequest) : this(
        action = getCalloutAction(request),
        parameters = CalloutParameters(getCalloutParams(request))
    )

    companion object {

        fun getCalloutParams(request: IdRequest): Set<CalloutParameter> =
            setOf<CalloutParameter>().apply {
                this.plus(CalloutParameter("projectId", request.projectId))
                this.plus(CalloutParameter("userId", request.userId))
                this.plus(CalloutParameter("moduleId", request.moduleId))
                this.plus(CalloutParameter("metadata", request.metadata))
                if(request.isVerifyRequest()) {
                    this.plus(CalloutParameter("verifyGuid", (request as IdVerifyRequest).verifyGuid))
                }
            }

        //StopShip: remove me when Callout is fully deprecated.
        fun getCalloutAction(request: IdRequest): CalloutAction =
            when(request){
                is IdEnrolRequest -> CalloutAction.REGISTER
                is IdIdentifyRequest -> CalloutAction.IDENTIFY
                is IdVerifyRequest -> CalloutAction.VERIFY
                else -> throw Throwable("different reqquest") //StopShip
            }

        fun Intent?.toCallout(): Callout =
            Callout(this.calloutAction, this.calloutParameters)
    }

}
