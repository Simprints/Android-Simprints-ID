package com.simprints.id.activities.login

import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.di.AppComponent
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class LoginPresenter(
    val view: LoginContract.View,
    component: AppComponent
) : LoginContract.Presenter {

    @Inject lateinit var crashReportManager: CrashReportManager

    init {
        component.inject(this)
    }

    override fun start() {}

    /**
     * Valid Scanned Text Format:
     * {"projectId":"someProjectId","projectSecret":"someSecret"}
     **/
    override fun processQRScannerAppResponse(scannedText: String) {
        try {
            val scannedJson = JSONObject(scannedText)
            val potentialProjectId = scannedJson.getString(PROJECT_ID_JSON_KEY)
            val potentialProjectSecret = scannedJson.getString(PROJECT_SECRET_JSON_KEY)
            view.updateProjectIdInTextView(potentialProjectId)
            view.updateProjectSecretInTextView(potentialProjectSecret)
            logMessageForCrashReportWithUITrigger("QR scanning successful")
        } catch (e: JSONException) {
            view.showErrorForInvalidQRCode()
            logMessageForCrashReportWithUITrigger("QR scanning unsuccessful")
        }
    }

    override fun logMessageForCrashReportWithUITrigger(message: String) {
        crashReportManager.logMessageForCrashReport(
            CrashReportTag.LOGIN,
            CrashReportTrigger.UI,
            message = message
        )
    }

    companion object {
        private const val PROJECT_ID_JSON_KEY = "projectId"
        private const val PROJECT_SECRET_JSON_KEY = "projectSecret"
    }
}
