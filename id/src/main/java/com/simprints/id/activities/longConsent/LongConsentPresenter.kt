package com.simprints.id.activities.longConsent

import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class LongConsentPresenter(val view: LongConsentContract.View,
                           component: AppComponent) : LongConsentContract.Presenter {

    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var preferences: PreferencesManager

    init {
        component.inject(this)
    }

    override fun start() {
        if (longConsentManager.checkIfLongConsentExists(preferences.language)) {
            val longConsentText = longConsentManager.getLongConsentText(preferences.language)
            view.setLongConsentText(longConsentText)
        } else {
            view.setNoPrivacyNoticeFound()
        }
    }

    override fun downloadLongConsent() {
        view.setDownloadInProgress(true)
        longConsentManager.downloadLongConsentWithProgress(preferences.language)
            .subscribeBy(
                onNext = {
                    view.setDownloadProgress(it)
                },
                onComplete = {
                    view.setDownloadInProgress(false)
                    view.setLongConsentText(longConsentManager.getLongConsentText(preferences.language))
                },
                onError = {
                    view.setDownloadInProgress(false)
                    view.showDownloadErrorToast()
                }
            )
    }
}
