package com.simprints.id.activities.longConsent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface LongConsentContract {

    interface View : BaseView<Presenter> {

        fun setLongConsentText(text: String)

        fun setNoPrivacyNoticeFound()

        fun setDownloadProgress(progress: Int)

        fun setDownloadInProgress(inProgress: Boolean)

        fun showDownloadErrorToast()
    }

    interface Presenter : BasePresenter {

        fun downloadLongConsent()

        fun logMessageForCrashReportWithUITrigger(message: String)
    }
}
