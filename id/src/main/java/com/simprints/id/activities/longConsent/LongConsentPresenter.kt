package com.simprints.id.activities.longConsent

import com.simprints.id.data.analytics.crashReport.CrashReportManager
import com.simprints.id.data.analytics.crashReport.CrashReportTags
import com.simprints.id.data.analytics.crashReport.CrashTrigger
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class LongConsentPresenter(val view: LongConsentContract.View,
                           component: AppComponent) : LongConsentContract.Presenter {

    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager

    init {
        component.inject(this)
    }

    override fun start() {
        if (longConsentManager.checkIfLongConsentExists(preferences.language)) {
            val longConsentText = longConsentManager.getLongConsentText(preferences.language)
            view.setLongConsentText(longConsentText)
            logMessageForCrashReportWithUITrigger("Long consent set for ${preferences.language}")
        } else {
            view.setNoPrivacyNoticeFound()
        }
    }

    override fun downloadLongConsent() {
        view.setDownloadInProgress(true)
        logMessageForCrashReportWithNetworkTrigger("Starting download for long consent")
        longConsentManager.downloadLongConsentWithProgress(preferences.language)
            .subscribeBy(
                onNext = {
                    view.setDownloadProgress(it)
                },
                onComplete = {
                    view.setDownloadInProgress(false)
                    view.setLongConsentText(longConsentManager.getLongConsentText(preferences.language))
                    logMessageForCrashReportWithNetworkTrigger("Successfully downloaded long consent")
                },
                onError = {
                    view.setDownloadInProgress(false)
                    view.showDownloadErrorToast()
                    logMessageForCrashReportWithNetworkTrigger("Error in downloading long consent")
                }
            )
    }

    override fun logMessageForCrashReportWithUITrigger(message: String) {
        crashReportManager.logInfo(CrashReportTags.LONG_CONSENT, CrashTrigger.UI, message)
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        crashReportManager.logInfo(CrashReportTags.LONG_CONSENT, CrashTrigger.NETWORK, message)
    }
}
