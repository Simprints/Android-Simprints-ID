package com.simprints.clientapi.activities.odk

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import com.simprints.clientapi.domain.responses.ErrorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class OdkActivity : RequestActivity(), OdkContract.View {

    companion object {
        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_SKIP_CHECK_KEY = "odk-skip-check"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
        private const val ODK_EXIT_REASON = "odk-exit-reason"
        private const val ODK_EXIT_EXTRA = "odk-exit-extra"
    }

    override val presenter: OdkContract.Presenter by inject { parametersOf(this, action) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadClientApiKoinModules()
        CoroutineScope(Dispatchers.Main).launch { presenter.start() }
    }

    override fun returnRegistration(registrationId: String, skipCheck: Boolean) = Intent().let {
        it.putExtra(ODK_REGISTRATION_ID_KEY, registrationId)
        it.putExtra(ODK_SKIP_CHECK_KEY, skipCheck)

        sendOkResult(it)
    }

    override fun returnIdentification(idList: String,
                                      confidenceList: String,
                                      tierList: String,
                                      sessionId: String,
                                      skipCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, idList)
        it.putExtra(ODK_CONFIDENCES_KEY, confidenceList)
        it.putExtra(ODK_TIERS_KEY, tierList)
        it.putExtra(ODK_SESSION_ID, sessionId)
        it.putExtra(ODK_SKIP_CHECK_KEY, skipCheck)

        sendOkResult(it)
    }

    override fun returnVerification(id: String, confidence: String, tier: String, skipCheck: Boolean) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, id)
        it.putExtra(ODK_CONFIDENCES_KEY, confidence)
        it.putExtra(ODK_TIERS_KEY, tier)
        it.putExtra(ODK_SKIP_CHECK_KEY, skipCheck)

        sendOkResult(it)
    }

    override fun returnExitForm(reason: String, extra: String, skipCheck: Boolean) = Intent().let {
        it.putExtra(ODK_EXIT_REASON, reason)
        it.putExtra(ODK_EXIT_EXTRA, extra)
        it.putExtra(ODK_SKIP_CHECK_KEY, skipCheck)

        sendOkResult(it)
    }

    override fun returnErrorToClient(errorResponse: ErrorResponse) = Intent().let {
        it.putExtra(ODK_SKIP_CHECK_KEY, errorResponse.skipCheckAfterError())
        sendOkResult(it)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
