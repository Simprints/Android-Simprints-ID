package com.simprints.id.activities.login.tools

import android.content.Intent
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.activities.login.response.QrCodeResponse
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.secure.securitystate.repository.SecurityStateRepository

class LoginActivityHelperImpl(
    private val securityStateRepository: SecurityStateRepository,
    private val jsonHelper: JsonHelper
) : LoginActivityHelper {

    override fun areMandatoryCredentialsPresent(
        projectId: String,
        projectSecret: String,
        userId: String
    ): Boolean {
        return projectId.isNotEmpty()
            && projectSecret.isNotEmpty()
            && userId.isNotEmpty()
    }

    override fun areSuppliedProjectIdAndProjectIdFromIntentEqual(
        suppliedProjectId: String,
        projectIdFromIntent: String
    ): Boolean {
        return suppliedProjectId == projectIdFromIntent
    }

    override fun tryParseQrCodeResponse(response: Intent): QrCodeResponse {
        val qrValue = response.getStringExtra(EXTRA_SCAN_RESULT)
        return qrValue?.let { jsonHelper.fromJson<QrCodeResponse>(it) } ?: throw Throwable("qrValue null")
    }

    override fun isSecurityStatusRunning(): Boolean {
        return securityStateRepository.getSecurityStatusFromLocal() == SecurityState.Status.RUNNING
    }

    private companion object {
        const val EXTRA_SCAN_RESULT = "SCAN_RESULT"
    }

}
