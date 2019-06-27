package com.simprints.clientapi.activities.odk

import android.content.Intent
import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.clientapi.di.KoinInjector.loadClientApiKoinModules
import com.simprints.clientapi.di.KoinInjector.unloadClientApiKoinModules
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class OdkActivity : RequestActivity(), OdkContract.View {

    companion object {
        private const val ODK_REGISTRATION_ID_KEY = "odk-registration-id"
        private const val ODK_GUIDS_KEY = "odk-guids"
        private const val ODK_CONFIDENCES_KEY = "odk-confidences"
        private const val ODK_TIERS_KEY = "odk-tiers"
        private const val ODK_SESSION_ID = "odk-session-id"
        private const val ODK_REFUSAL_REASON = "odk-refusal-reason"
        private const val ODK_REFUSAL_EXTRA = "odk-refusal-extra"
    }

    override val presenter: OdkContract.Presenter by inject { parametersOf(this, action) }

    override val guidSelectionNotifier = OdkGuidSelectionNotifier(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadClientApiKoinModules()
        CoroutineScope(Dispatchers.Main).launch { presenter.start() }
    }

    override fun returnRegistration(registrationId: String) = Intent().let {
        it.putExtra(ODK_REGISTRATION_ID_KEY, registrationId)
        sendOkResult(it)
    }

    override fun returnIdentification(idList: String,
                                      confidenceList: String,
                                      tierList: String,
                                      sessionId: String) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, idList)
        it.putExtra(ODK_CONFIDENCES_KEY, confidenceList)
        it.putExtra(ODK_TIERS_KEY, tierList)
        it.putExtra(ODK_SESSION_ID, sessionId)
        sendOkResult(it)
    }

    override fun returnVerification(id: String, confidence: String, tier: String) = Intent().let {
        it.putExtra(ODK_GUIDS_KEY, id)
        it.putExtra(ODK_CONFIDENCES_KEY, confidence)
        it.putExtra(ODK_TIERS_KEY, tier)
        sendOkResult(it)
    }

    override fun returnRefusalForm(reason: String, extra: String) = Intent().let {
        it.putExtra(ODK_REFUSAL_REASON, reason)
        it.putExtra(ODK_REFUSAL_EXTRA, extra)
        sendOkResult(it)
    }

    override fun onDestroy() {
        super.onDestroy()
        unloadClientApiKoinModules()
    }

}
