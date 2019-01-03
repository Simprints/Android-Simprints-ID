package com.simprints.clientapi.activities.odk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.activities.ClientRequestActivity
import com.simprints.clientapi.clientrequests.extractors.EnrollmentExtractor
import com.simprints.clientapi.clientrequests.extractors.VerifyExtractor
import com.simprints.clientapi.routers.ClientRequestErrorRouter.routeClientRequestError
import com.simprints.clientapi.routers.SimprintsRequestRouter.IDENTIFY_REQUEST_CODE
import com.simprints.clientapi.routers.SimprintsRequestRouter.REGISTER_REQUEST_CODE
import com.simprints.clientapi.routers.SimprintsRequestRouter.VERIFY_REQUEST_CODE
import com.simprints.clientapi.routers.SimprintsRequestRouter.routeSimprintsRequest
import com.simprints.clientapi.simprintsrequests.SimprintsIdRequest
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification


class OdkActivity : AppCompatActivity(), OdkContract.View, ClientRequestActivity {

    companion object {
        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
    }

    override lateinit var presenter: OdkContract.Presenter
    override lateinit var enrollmentExtractor: EnrollmentExtractor
    override lateinit var verifyExtractor: VerifyExtractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enrollmentExtractor = EnrollmentExtractor(intent)
        verifyExtractor = VerifyExtractor(intent)
        presenter = OdkPresenter(this, intent.action).apply { start() }
    }

    override fun returnActionErrorToClient() {
        setResult(SIMPRINTS_INVALID_INTENT_ACTION, intent)
        finish()
    }

    override fun requestIdentifyCallout() {
        val identifyIntent = Intent(SIMPRINTS_IDENTIFY_INTENT).apply { putExtras(intent) }
        startActivityForResult(identifyIntent, IDENTIFY_REQUEST_CODE)
    }

    override fun requestVerifyCallout() {
        val verifyIntent = Intent(SIMPRINTS_VERIFY_INTENT).apply { putExtras(intent) }
        startActivityForResult(verifyIntent, VERIFY_REQUEST_CODE)
    }

    override fun requestConfirmIdentityCallout() {
        startService(Intent(SIMPRINTS_SELECT_GUID_INTENT).apply {
            putExtras(intent)
            setPackage(Constants.SIMPRINTS_PACKAGE_NAME)
        })
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK || data == null)
            setResult(resultCode, data).also { finish() }
        else
            when (requestCode) {
                REGISTER_REQUEST_CODE -> presenter.processRegistration(
                    data.getParcelableExtra(SIMPRINTS_REGISTRATION)
                )
                IDENTIFY_REQUEST_CODE -> presenter.processIdentification(
                    data.getParcelableArrayListExtra<Identification>(SIMPRINTS_IDENTIFICATIONS),
                    data.getStringExtra(SIMPRINTS_SESSION_ID)
                )
                VERIFY_REQUEST_CODE -> presenter.processVerification(
                    data.getParcelableExtra(SIMPRINTS_VERIFICATION)
                )
                else -> presenter.processReturnError()
            }
    }

    override fun returnRegistration(registrationId: String) = Intent().let {
        it.putExtra(ODK_REGISTRATION_ID_KEY, registrationId)
        sendOkResult(it)
    }

    override fun returnIdentification(idList: String, confidenceList: String, tierList: String, sessionId: String) =
        Intent().let {
            it.putExtra(ODK_GUIDS_KEY, idList)
            it.putExtra(ODK_CONFIDENCES_KEY, confidenceList)
            it.putExtra(ODK_TIERS_KEY, tierList)
            it.putExtra(ODK_SESSION_ID, sessionId)
            sendOkResult(it)
        }

    override fun returnVerification(id: String, confidence: String, tier: String) =
        Intent().let {
            it.putExtra(ODK_GUIDS_KEY, id)
            it.putExtra(ODK_CONFIDENCES_KEY, confidence)
            it.putExtra(ODK_TIERS_KEY, tier)
            sendOkResult(it)
        }

    override fun showErrorForException(exception: Exception) {
        routeClientRequestError(this, exception)
        finish()
    }

    override fun sendSimprintsRequest(request: SimprintsIdRequest) =
        routeSimprintsRequest(this, request)

    private fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
