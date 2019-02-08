package com.simprints.id.activities.longConsent

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.AnalyticsTags
import com.simprints.id.data.analytics.LogPrompter
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class LongConsentPresenter(val view: LongConsentContract.View,
                           component: AppComponent) : LongConsentContract.Presenter {

    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var anayticsManager: AnalyticsManager

    init {
        component.inject(this)
    }

    override fun start() {
        if (longConsentManager.checkIfLongConsentExists(preferences.language)) {
            val longConsentText = longConsentManager.getLongConsentText(preferences.language)
            view.setLongConsentText(longConsentText)
            logMessageToAnalyticsWithUIPrompt("Long consent set for ${preferences.language}")
        } else {
            view.setNoPrivacyNoticeFound()
        }
    }

    override fun downloadLongConsent() {
        view.setDownloadInProgress(true)
        logMessageToAnalyticsWithNetworkPrompt("Starting download for long consent")
        longConsentManager.downloadLongConsentWithProgress(preferences.language)
            .subscribeBy(
                onNext = {
                    view.setDownloadProgress(it)
                },
                onComplete = {
                    view.setDownloadInProgress(false)
                    view.setLongConsentText(longConsentManager.getLongConsentText(preferences.language))
                    logMessageToAnalyticsWithNetworkPrompt("Successfully downloaded long consent")
                },
                onError = {
                    view.setDownloadInProgress(false)
                    view.showDownloadErrorToast()
                    logMessageToAnalyticsWithNetworkPrompt("Error in downloading long consent")
                }
            )
    }

    override fun logMessageToAnalyticsWithUIPrompt(message: String) {
        anayticsManager.logInfo(AnalyticsTags.LONG_CONSENT, LogPrompter.UI, message)
    }

    private fun logMessageToAnalyticsWithNetworkPrompt(message: String) {
        anayticsManager.logInfo(AnalyticsTags.LONG_CONSENT, LogPrompter.NETWORK, message)
    }
}
