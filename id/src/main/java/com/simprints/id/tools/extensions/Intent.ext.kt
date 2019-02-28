package com.simprints.id.tools.extensions

import android.content.Intent
import com.simprints.clientapi.simprintsrequests.requests.ClientApiConfirmIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiEnrollRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiIdentifyRequest
import com.simprints.clientapi.simprintsrequests.requests.ClientApiVerifyRequest
import com.simprints.id.domain.requests.*

fun Intent.parseClientApiRequest(): AppBaseRequest =
    this.extras?.let {
        try {
            when (it.keySet().first()) {
                ClientApiEnrollRequest.BUNDLE_KEY ->
                    it.getParcelable<ClientApiEnrollRequest>(ClientApiEnrollRequest.BUNDLE_KEY)?.toDomainIdEnrolRequest()
                ClientApiIdentifyRequest.BUNDLE_KEY ->
                    it.getParcelable<ClientApiIdentifyRequest>(ClientApiIdentifyRequest.BUNDLE_KEY)?.toDomainIdIdentifyRequest()
                ClientApiVerifyRequest.BUNDLE_KEY ->
                    it.getParcelable<ClientApiVerifyRequest>(ClientApiVerifyRequest.BUNDLE_KEY)?.toDomainIdVerifyRequest()
                ClientApiConfirmIdentifyRequest.BUNDLE_KEY ->
                    it.getParcelable<ClientApiConfirmIdentifyRequest>(ClientApiConfirmIdentifyRequest.BUNDLE_KEY)?.toDomainIdConfirmIdentifyRequest()
                else -> null
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            throw Throwable("no extra") //StopShip
        }
    } ?: throw Throwable("no extra") //StopShip
