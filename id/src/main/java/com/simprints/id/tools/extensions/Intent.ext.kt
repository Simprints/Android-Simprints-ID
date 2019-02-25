package com.simprints.id.tools.extensions

import android.content.Intent
import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest
import com.simprints.id.domain.request.IdRequest
import com.simprints.id.domain.request.toIdDomainEnrolRequest
import com.simprints.id.domain.request.toIdDomainIdIdentifyRequest
import com.simprints.id.domain.request.toIdDomainIdVerifyRequest

fun Intent.parseClientApiRequest(): IdRequest =
    this.extras?.let {
        try {
            when (it.keySet().first()) {
                ClientApiEnrollRequest.REQUEST_NAME ->
                    it.getParcelable<ClientApiEnrollRequest>(ClientApiEnrollRequest.REQUEST_NAME)?.toIdDomainEnrolRequest()
                ClientApiIdentifyRequest.REQUEST_NAME ->
                    it.getParcelable<ClientApiIdentifyRequest>(ClientApiIdentifyRequest.REQUEST_NAME)?.toIdDomainIdIdentifyRequest()
                ClientApiVerifyRequest.REQUEST_NAME ->
                    it.getParcelable<ClientApiVerifyRequest>(ClientApiVerifyRequest.REQUEST_NAME)?.toIdDomainIdVerifyRequest()
                else -> null
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            throw Throwable("no extra") //StopShip
        }
    } ?: throw Throwable("no extra") //StopShip
