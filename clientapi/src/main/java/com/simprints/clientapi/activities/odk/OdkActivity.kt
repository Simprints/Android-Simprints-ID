package com.simprints.clientapi.activities.odk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.simprints.clientapi.exceptions.InvalidProjectIdException
import com.simprints.clientapi.validators.EnrollmentValidator
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.Identification

class OdkActivity : AppCompatActivity(), OdkContract.View {

    companion object {
        private const val REGISTER_REQUEST_CODE = 97
        private const val IDENTIFY_REQUEST_CODE = 98
        private const val VERIFY_REQUEST_CODE = 99

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

    override fun returnActionErrorToClient() {
        setResult(SIMPRINTS_INVALID_INTENT_ACTION, intent)
        finish()
    }

    override fun requestRegisterCallout() {
        try {
            EnrollmentValidator(intent).validateClientRequest()
            val registerIntent = Intent(SIMPRINTS_REGISTER_INTENT).apply { putExtras(intent) }
            startActivityForResult(registerIntent, REGISTER_REQUEST_CODE)
        } catch (ex: InvalidProjectIdException) {
            // TODO: map to error screen
            Log.d("", ex.toString())
        }
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

    private fun sendOkResult(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
