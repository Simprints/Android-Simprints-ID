package com.simprints.id.orchestrator.modality.flows

import android.app.Activity
import android.content.Intent
import com.simprints.id.domain.moduleapi.app.requests.AppRequest
import com.simprints.id.domain.moduleapi.face.FaceRequestFactory.buildFaceRequest
import com.simprints.id.domain.moduleapi.face.FaceToDomainResponse.fromFaceToDomainResponse
import com.simprints.id.domain.moduleapi.face.requests.DomainToFaceRequest.fromDomainToFaceRequest
import com.simprints.id.orchestrator.modality.flows.interfaces.ModalityFlow.*
import com.simprints.id.orchestrator.modality.flows.interfaces.SingleModalityFlow
import com.simprints.moduleapi.face.requests.IFaceRequest
import com.simprints.moduleapi.face.responses.IFaceResponse
import java.lang.IllegalArgumentException

class FaceModalityFlow(private val appRequest: AppRequest,
                       private val packageName: String) : SingleModalityFlow {

    companion object {
        const val faceActivityClassName = "com.simprints.face.activities.FaceCaptureActivity"
        const val REQUEST_CODE_FACE = 1
    }

    override val steps= linkedMapOf<Int, Step?> (REQUEST_CODE_FACE to null)

    override val nextRequest: Request?
        get(){
            val stepNotStartedYet =
                steps.entries.find { it.value == null || it.value?.response == null } // First step not started or waiting for response
            return if(stepNotStartedYet != null) {

                // Already build the request - waiting for the response
                stepNotStartedYet.value?.request ?: run {

                    // Never built the request yet
                    val requestCode = stepNotStartedYet.key
                    getModalityStepRequests(requestCode).also {
                        steps[requestCode] = Step(it)
                    }
                }
            } else {
                null
            }
        }

    private fun getModalityStepRequests(requestCode: Int): Request =
        if(requestCode == REQUEST_CODE_FACE) {
            getModalityStepRequestForFace()
        } else {
            throw IllegalArgumentException("") //StopShip: Handle it
        }


    private fun getModalityStepRequestForFace(): Request {
        val intent = Intent().setClassName(packageName, faceActivityClassName)
        intent.putExtra(IFaceRequest.BUNDLE_KEY, fromDomainToFaceRequest(buildFaceRequest(appRequest)))
        return Request(REQUEST_CODE_FACE, intent)
    }

    override fun handleIntentResult(requestCode: Int, resultCode: Int, data: Intent?): Response? =
        try {
            require(resultCode == Activity.RESULT_OK)
            require(requestCode == REQUEST_CODE_FACE)
            require(data != null)

            val potentialFaceResponse = data.getParcelableExtra<IFaceResponse>(IFaceResponse.BUNDLE_KEY)
            fromFaceToDomainResponse(potentialFaceResponse).also {
                steps[requestCode]?.response = it
            }
        } catch (t: Throwable) {
            if(t !is IllegalArgumentException) {
                t.printStackTrace()
            }
            null
        }
}
