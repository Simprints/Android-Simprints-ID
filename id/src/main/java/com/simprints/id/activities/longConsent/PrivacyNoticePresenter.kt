package com.simprints.id.activities.longConsent

import android.annotation.SuppressLint
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.analytics.crashreport.CrashReportTag
import com.simprints.id.data.analytics.crashreport.CrashReportTrigger
import com.simprints.id.data.consent.LongConsentManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.tools.device.DeviceManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PrivacyNoticePresenter(val view: PrivacyNoticeContract.View,
                             component: AppComponent) : PrivacyNoticeContract.Presenter {

    @Inject lateinit var longConsentManager: LongConsentManager
    @Inject lateinit var preferences: PreferencesManager
    @Inject lateinit var crashReportManager: CrashReportManager
    @Inject lateinit var deviceManager: DeviceManager

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
        val isConnected = runBlocking { deviceManager.isConnected() }
        if (isConnected) {
            logMessageForCrashReportWithNetworkTrigger("Starting download for long consent")
            view.setDownloadInProgress(true)
            startDownloadingLongConsent()
        } else {
            view.showUserOfflineToast()
        }
    }

    @SuppressLint("CheckResult")
    private fun startDownloadingLongConsent() {
        longConsentManager.downloadLongConsentWithProgress(preferences.language)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
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
        crashReportManager.logMessageForCrashReport(CrashReportTag.LONG_CONSENT, CrashReportTrigger.UI, message = message)
    }

    private fun logMessageForCrashReportWithNetworkTrigger(message: String) {
        crashReportManager.logMessageForCrashReport(CrashReportTag.LONG_CONSENT, CrashReportTrigger.NETWORK, message = message)
    }
}
