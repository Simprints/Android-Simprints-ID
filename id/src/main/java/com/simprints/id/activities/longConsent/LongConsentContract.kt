package com.simprints.id.activities.longConsent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface LongConsentContract {

    interface View : BaseView<Presenter> {

        var showProgressBar: Boolean

        fun setLongConsentText(text: String)

        fun setDefaultLongConsent()

    }

    interface Presenter : BasePresenter
}
