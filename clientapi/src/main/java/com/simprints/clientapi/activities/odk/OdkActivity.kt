package com.simprints.clientapi.activities.odk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.routers.SimprintsRequestRouter.IDENTIFY_REQUEST_CODE
import com.simprints.clientapi.routers.SimprintsRequestRouter.REGISTER_REQUEST_CODE
import com.simprints.clientapi.routers.SimprintsRequestRouter.VERIFY_REQUEST_CODE
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification


class OdkActivity : RequestActivity(), OdkContract.View {

    companion object {
        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
    }

    override lateinit var presenter: OdkContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = OdkPresenter(this, intent.action).apply { start() }
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

    private fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
